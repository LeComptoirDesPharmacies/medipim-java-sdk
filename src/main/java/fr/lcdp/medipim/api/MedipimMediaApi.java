package fr.lcdp.medipim.api;

import fr.lcdp.medipim.api.client.Client;
import fr.lcdp.medipim.api.client.Response;
import fr.lcdp.medipim.api.entities.media.MedipimMediaType;
import fr.lcdp.medipim.api.entities.media.MedipimPhoto;
import fr.lcdp.medipim.api.query.PaginatedResponse;
import fr.lcdp.medipim.api.query.QueryPage;
import fr.lcdp.medipim.api.query.SortingValue;
import fr.lcdp.medipim.api.query.media.Query;
import fr.lcdp.medipim.api.query.media.QueryFilter;
import fr.lcdp.medipim.api.query.media.QuerySorting;
import com.fasterxml.jackson.databind.JsonNode;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

@Singleton
public class MedipimMediaApi extends MedipimApi {

    @Inject
    public MedipimMediaApi(Client ws, String baseUrl, String username, String password) {
        super(ws, baseUrl, username, password);
    }



    private <T> PaginatedResponse<T> postMediaQuery(JsonNode query, Class<T> toValueType) {
        try {
            Response response = this.createAuthenticatedRequest("/v4/media/query")
                    .post(query)
                    .toCompletableFuture()
                    .get();

            if (response.getStatus() != 200) {
                throw new RuntimeException(String.format("Medipim API returned status %s - %s", response.getStatus(), response.getBody()));
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
            throw new RuntimeException(e);
        }
    }

    public PaginatedResponse<MedipimPhoto> getModifiedPhotosSince(OffsetDateTime updatedAtGe) {
        return postMediaQuery(buildGetModifiedPhotosSinceQuery(updatedAtGe), MedipimPhoto.class);
    }

    public JsonNode buildGetModifiedPhotosSinceQuery(OffsetDateTime updatedAtGe) {

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

        QueryPage page = new QueryPage(0, 250);

        Query query = new Query(
                filter,
                sorting,
                page
        );

        return this.serialize(query);
    }
}
