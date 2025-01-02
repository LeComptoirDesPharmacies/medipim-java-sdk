package fr.lecomptoirdespharmacies.medipim.api;

import com.fasterxml.jackson.databind.JsonNode;
import fr.lecomptoirdespharmacies.medipim.api.client.Client;
import fr.lecomptoirdespharmacies.medipim.api.client.Response;
import fr.lecomptoirdespharmacies.medipim.api.entities.products.MedipimProduct;
import fr.lecomptoirdespharmacies.medipim.api.query.PaginatedResponse;
import fr.lecomptoirdespharmacies.medipim.api.query.QueryFilterHasContent;
import fr.lecomptoirdespharmacies.medipim.api.query.QueryPage;
import fr.lecomptoirdespharmacies.medipim.api.query.SortingValue;
import fr.lecomptoirdespharmacies.medipim.api.query.products.Query;
import fr.lecomptoirdespharmacies.medipim.api.query.products.QueryFilter;
import fr.lecomptoirdespharmacies.medipim.api.query.products.QuerySorting;
import fr.lecomptoirdespharmacies.medipim.exceptions.RateLimitException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class MedipimProductsApi extends MedipimApi {
    public MedipimProductsApi(Client ws, String baseUrl, String username, String password) {
        super(ws, baseUrl, username, password);
    }

    private List<MedipimProduct> streamToProducts(Response response) {
        if (!Arrays.asList(200, 204).contains(response.getStatus())) {
            String message = String.format("Medipim API returned status %s - %s", response.getStatus(), response.getBody());
            if (Objects.equals(response.getStatus(), 429)) {
                throw new RateLimitException(message);
            }
            throw new RuntimeException(message);
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
        return searchProductById(id, null);
    }

    public MedipimProduct searchProductById(String id, Duration timeout) {
        try {
            MedipimProduct product = this.createAuthenticatedRequest("/v4/products/find")
                    .setRequestTimeout(timeout)
                    .addQueryParameter("id", id)
                    .get()
                    .thenApply(response -> {
                        if (response.getStatus() != 200) {
                            if (Objects.equals(response.getStatus(), 429)) {
                                String message = String.format("Medipim API returned status %s - %s", response.getStatus(), response.getBody());
                                throw new RateLimitException(message);
                            }
                            // Product not found
                            return null;
                        }
                        JsonNode result = response.asJson().get("product");

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
        return postProductStream(query, null);
    }

    private List<MedipimProduct> postProductStream(JsonNode query, Duration timeout) {
        try {
            List<MedipimProduct> products = this.createAuthenticatedRequest("/v4/products/stream")
                    .setRequestTimeout(timeout)
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
        return postProductsQuery(query, null);
    }

    private PaginatedResponse<MedipimProduct> postProductsQuery(JsonNode query, Duration timeout) {
        try {
            Response response = this.createAuthenticatedRequest("/v4/products/query")
                    .setRequestTimeout(timeout)
                    .post(query)
                    .toCompletableFuture()
                    .get();

            if (response.getStatus() != 200) {
                String message = String.format("Medipim API returned status %s - %s", response.getStatus(), response.getBody());
                if (Objects.equals(response.getStatus(), 429)) {
                    throw new RateLimitException(message);
                }
                throw new RuntimeException(message);
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
        return searchProductByBarcode(barcode, null);
    }

    public MedipimProduct searchProductByBarcode(String barcode, Duration timeout) {
        return getMostMatchedMedipimProduct(
                postProductStream(
                        buildSearchProductByBarcodeQuery(barcode),
                        timeout
                ),
                barcode
        );
    }

    private MedipimProduct getMostMatchedMedipimProduct(List<MedipimProduct> medipimProducts, String barcode) {
        /**
         * We shoud remove products where the searched barcode is not present in product codes
         * because we truncate the search to 7 characters, and it can bring us to wrong products
         */
        return medipimProducts
                .stream()
                .filter(p ->
                        Objects.equals(p.cipOrAcl7(), barcode) ||
                                Objects.equals(p.cip13(), barcode) ||
                                Objects.equals(p.acl13(), barcode) ||
                                CollectionUtils.emptyIfNull(p.ean()).contains(barcode)
                )
                .findFirst()
                .orElse(null);
    }

    private JsonNode buildSearchProductByBarcodeQuery(String barcode) {

        List<QueryFilter> barcodeFilters = new ArrayList<>();


        if (StringUtils.isNumeric(barcode) &&
                StringUtils.startsWith(barcode, "3400") &&
                StringUtils.length(barcode) >= 13) {
            barcodeFilters.add(
                    new QueryFilter.QueryFilterBuilder()
                            .cip13(StringUtils.left(barcode, 13))
                            .build()
            );
        }

        if (StringUtils.isNumeric(barcode) &&
                StringUtils.startsWith(barcode, "3401") &&
                StringUtils.length(barcode) >= 13
        ) {
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

        if (StringUtils.isNumeric(barcode) &&
                !Objects.equals(barcode, "0")// cipOrAcl7 consider the value '0' as empty value. (See : LDS-3337)
        ) {
            barcodeFilters.add(
                    new QueryFilter.QueryFilterBuilder()
                            .cipOrAcl7(StringUtils.left(barcode, 7))
                            .build()
            );
        }


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
        return getProductsByMediaIds(mediaIds, null);
    }

    public List<MedipimProduct> getProductsByMediaIds(List<Long> mediaIds, Duration timeout) {
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


        return postProductStream(this.serialize(query), timeout);
    }

    public PaginatedResponse<MedipimProduct> getModifiedProductSince(OffsetDateTime updatedAtGe,
                                                                     boolean containMedia) {
        return getModifiedProductSince(updatedAtGe, containMedia, null);
    }

    public PaginatedResponse<MedipimProduct> getModifiedProductSince(OffsetDateTime updatedAtGe,
                                                                     boolean containMedia, Duration timeout) {
        return postProductsQuery(buildGetModifiedProductSinceQuery(updatedAtGe, containMedia), timeout);
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
