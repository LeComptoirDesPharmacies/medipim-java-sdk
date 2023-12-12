package fr.lcdp.medipim.api;

import fr.lcdp.medipim.api.client.Client;
import fr.lcdp.medipim.api.client.Response;
import fr.lcdp.medipim.api.entities.products.MedipimProduct;
import fr.lcdp.medipim.api.query.PaginatedResponse;
import fr.lcdp.medipim.api.query.QueryFilterHasContent;
import fr.lcdp.medipim.api.query.QueryPage;
import fr.lcdp.medipim.api.query.SortingValue;
import fr.lcdp.medipim.api.query.products.Query;
import fr.lcdp.medipim.api.query.products.QueryFilter;
import fr.lcdp.medipim.api.query.products.QuerySorting;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Singleton
public class MedipimProductsApi extends MedipimApi {


    @Inject
    public MedipimProductsApi(Client ws, String baseUrl, String username, String password) {
        super(ws, baseUrl, username, password);
    }

    private List<MedipimProduct> streamToProducts(Response response) {
        if (!Arrays.asList(200, 204).contains(response.getStatus())) {
            throw new RuntimeException(String.format("Medipim API returned status %s - %s", response.getStatus(), response.getBody()));
        }

        List<JsonNode> results = this.readStream(response)
                .map(jsonNode -> jsonNode.get("result"))
                /**
                 * If result is 204, we got empty body and the preceding line will return an array of one null item.
                 * Filter them to get empty array
                 */
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return this.deserializeList(results, MedipimProduct.class);
    }

    public MedipimProduct searchProductById(String id) {
        try {
            MedipimProduct product = this.createAuthenticatedRequest("/v4/products/find")
                    .addQueryParameter("id", id)
                    .get()
                    .thenApply(response -> {
                        if (response.getStatus() != 200) {
                            // Product not found
                            return null;
                        }
                        JsonNode result = response.asJson().get("product");;
                        return this.deserialize(result, MedipimProduct.class);
                    })
                    .toCompletableFuture()
                    .get();

            return product;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private List<MedipimProduct> postProductStream(JsonNode query) {
        try {
            List<MedipimProduct> products = this.createAuthenticatedRequest("/v4/products/stream")
                    .post(query)
                    .thenApply(this::streamToProducts)
                    .toCompletableFuture()
                    .get();

            return products;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }



    private PaginatedResponse<MedipimProduct> postProductsQuery(JsonNode query) {
        try {
            Response response = this.createAuthenticatedRequest("/v4/products/query")
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

            List<MedipimProduct> products = this.deserializeList(results, MedipimProduct.class);

            return new PaginatedResponse(
                    isExhaustive,
                    products
            );

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    public MedipimProduct searchProductByBarcode(String barcode) {
        return postProductStream(buildSearchProductByBarcodeQuery(barcode))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private JsonNode buildSearchProductByBarcodeQuery(String barcode) {

        List<QueryFilter> barcodeFilters = new ArrayList<>();


        if (StringUtils.startsWith(barcode, "3400")) {
            barcodeFilters.add(
                    new QueryFilter.QueryFilterBuilder()
                            .cip13(StringUtils.left(barcode, 13))
                            .build()
            );
        }

        if (StringUtils.startsWith(barcode, "3401")) {
            barcodeFilters.add(
                    new QueryFilter.QueryFilterBuilder()
                            .acl13(StringUtils.left(barcode, 13))
                            .build()
            );
        }

        barcodeFilters.add(
                new QueryFilter.QueryFilterBuilder()
                        .ean(barcode)
                        .build()
        );

        barcodeFilters.add(
                new QueryFilter.QueryFilterBuilder()
                        .cipOrAcl7(StringUtils.left(barcode, 7))
                        .build()
        );


        QueryFilter filter = new QueryFilter.QueryFilterBuilder()
                .or(barcodeFilters).build();

        QuerySorting sorting = new QuerySorting.QuerySortingBuilder()
                .id(SortingValue.DESC)
                .build();

        Query query = new Query(
                filter,
                sorting,
                null
        );

        return this.serialize(query);
    }

    public List<MedipimProduct> getProductsByMediaIds(List<Long> mediaIds) {
        QueryFilter filter = new QueryFilter.QueryFilterBuilder()
                .media(mediaIds)
                .build();

        QuerySorting sorting = new QuerySorting.QuerySortingBuilder()
                .id(SortingValue.DESC)
                .build();

        Query query = new Query(
                filter,
                sorting,
                null
        );


        return postProductStream(this.serialize(query));
    }

    public PaginatedResponse<MedipimProduct> getModifiedProductSince(OffsetDateTime updatedAtGe,
                                                                     boolean containMedia) {
        return postProductsQuery(buildGetModifiedProductSinceQuery(updatedAtGe, containMedia));
    }

    public JsonNode buildGetModifiedProductSinceQuery(OffsetDateTime updatedAtGe,
                                                      boolean containMedia) {

        List<QueryFilter> filters = new ArrayList<>();

        if (containMedia) {
            filters.add(
                    new QueryFilter.QueryFilterBuilder()
                            .hasContent(new QueryFilterHasContent.QueryFilterHasContentBuilder()
                                    .flag("media")
                                    .locale("fr")
                                    .build())
                            .build()
            );

        }

        filters.add(
                new QueryFilter.QueryFilterBuilder()
                        .updatedSince(updatedAtGe.toEpochSecond())
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
