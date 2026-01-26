package fr.lecomptoirdespharmacies.medipim.api;

import com.fasterxml.jackson.databind.JsonNode;
import fr.lecomptoirdespharmacies.medipim.api.client.Client;
import fr.lecomptoirdespharmacies.medipim.api.client.Response;
import fr.lecomptoirdespharmacies.medipim.api.entities.media.MedipimMediaType;
import fr.lecomptoirdespharmacies.medipim.api.entities.media.MedipimPhoto;
import fr.lecomptoirdespharmacies.medipim.api.query.PaginatedResponse;
import fr.lecomptoirdespharmacies.medipim.api.query.QueryPage;
import fr.lecomptoirdespharmacies.medipim.api.query.SortingValue;
import fr.lecomptoirdespharmacies.medipim.api.query.media.Query;
import fr.lecomptoirdespharmacies.medipim.api.query.media.QueryFilter;
import fr.lecomptoirdespharmacies.medipim.api.query.media.QuerySorting;
import fr.lecomptoirdespharmacies.medipim.exceptions.MedipimException;
import fr.lecomptoirdespharmacies.medipim.exceptions.RateLimitException;
import fr.lecomptoirdespharmacies.medipim.exceptions.UnexpectedStatusCodeException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MedipimMediaApi extends MedipimApi {

    public MedipimMediaApi(Client ws, String baseUrl, String username, String password) {
        super(ws, baseUrl, username, password);
    }


    private <T> PaginatedResponse<T> postMediaQuery(JsonNode query, Class<T> toValueType) {
        return postMediaQuery(query, toValueType, null);
    }

    private <T> PaginatedResponse<T> postMediaQuery(JsonNode query, Class<T> toValueType, Duration timeout) {
        try {
            Response response = this.createAuthenticatedRequest("/v4/media/query")
                    .setRequestTimeout(timeout)
                    .post(query)
                    .toCompletableFuture()
                    .get();

            if (response.getStatus() != 200) {
                String message = String.format("Medipim API returned status %s - %s", response.getStatus(), response.getBody());
                if (Objects.equals(response.getStatus(), 429)) {
                    throw new RateLimitException(message);
                }
                throw new UnexpectedStatusCodeException(message);
            }

            JsonNode jsonResponse = response.asJson();
            JsonNode meta = jsonResponse.get("meta");
            JsonNode results = jsonResponse.get("results");

            long totalRecord = meta.get("total").asLong();
            JsonNode page = meta.get("page");
            long pageOffset = page.get("offset").asLong();
            long pageSize = page.get("size").asLong();
            boolean isExhaustive = totalRecord <= pageOffset + pageSize;

            List<T> photos = this.deserializeList(results, toValueType);

            return new PaginatedResponse(
                    isExhaustive,
                    photos
            );

        } catch (InterruptedException | ExecutionException e) {
            throw new MedipimException(e);
        }
    }

    public PaginatedResponse<MedipimPhoto> getModifiedPhotosSince(OffsetDateTime updatedAtGe) {
        return getModifiedPhotosSince(updatedAtGe, null, null);
    }

    public PaginatedResponse<MedipimPhoto> getModifiedPhotosSince(OffsetDateTime updatedAtGe, Duration timeout) {
        return getModifiedPhotosSince(updatedAtGe, null, timeout);
    }

    public PaginatedResponse<MedipimPhoto> getModifiedPhotosSince(OffsetDateTime updatedAtGe, QueryPage page) {
        return getModifiedPhotosSince(updatedAtGe, page, null);
    }

    public PaginatedResponse<MedipimPhoto> getModifiedPhotosSince(OffsetDateTime updatedAtGe, QueryPage page, Duration timeout) {
        return postMediaQuery(buildGetModifiedPhotosSinceQuery(updatedAtGe, page), MedipimPhoto.class, timeout);
    }

    public JsonNode buildGetModifiedPhotosSinceQuery(OffsetDateTime updatedAtGe, QueryPage page) {

        List<QueryFilter> filters = new ArrayList<>();

        filters.add(
                new QueryFilter.QueryFilterBuilder()
                        .type(MedipimMediaType.photo)
                        .build()
        );
        filters.add(
                new QueryFilter.QueryFilterBuilder()
                        .touchedAt(updatedAtGe.toEpochSecond())
                        .build()
        );
        filters.add(
                new QueryFilter.QueryFilterBuilder()
                        .locale(Locale.FRENCH)
                        .build()
        );
        QueryFilter filter = new QueryFilter.QueryFilterBuilder()
                .and(filters)
                .build();

        QuerySorting sorting = new QuerySorting.QuerySortingBuilder()
                .touchedAt(SortingValue.ASC)
                .build();

        QueryPage effectivePage = page != null ? page : new QueryPage(0, 250);

        Query query = new Query(
                filter,
                sorting,
                effectivePage
        );

        return this.serialize(query);
    }
}
