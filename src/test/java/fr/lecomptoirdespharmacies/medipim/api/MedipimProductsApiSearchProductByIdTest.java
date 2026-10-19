package fr.lecomptoirdespharmacies.medipim.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.lecomptoirdespharmacies.medipim.api.client.Client;
import fr.lecomptoirdespharmacies.medipim.api.client.Request;
import fr.lecomptoirdespharmacies.medipim.api.client.Response;
import fr.lecomptoirdespharmacies.medipim.api.entities.products.MedipimProduct;
import fr.lecomptoirdespharmacies.medipim.exceptions.MedipimException;
import fr.lecomptoirdespharmacies.medipim.exceptions.RateLimitException;
import fr.lecomptoirdespharmacies.medipim.exceptions.UnexpectedStatusCodeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;

class MedipimProductsApiSearchProductByIdTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static MedipimProductsApi apiAnswering(int status, String body) {
        Response response = new Response() {
            @Override
            public int getStatus() {
                return status;
            }

            @Override
            public String getBody() {
                return body;
            }

            @Override
            public JsonNode asJson() {
                try {
                    return MAPPER.readTree(body);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        Request request = new Request() {
            @Override
            public Request setAuth(String username, String password) {
                return this;
            }

            @Override
            public Request addQueryParameter(String name, String value) {
                return this;
            }

            @Override
            public CompletionStage<Response> get() {
                return CompletableFuture.completedFuture(response);
            }

            @Override
            public CompletionStage<Response> post(JsonNode body) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Request setRequestTimeout(Duration timeout) {
                return this;
            }
        };
        Client client = url -> request;
        return new MedipimProductsApi(client, "https://api.medipim.test", "user", "password");
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    @Test
    void returnsTheProductOn200() {
        MedipimProduct product = apiAnswering(200, "{\"product\": {\"id\": \"M123\"}}").searchProductById("M123");

        assertNotNull(product);
        assertEquals("M123", product.id());
    }

    @Test
    void returnsNullOnlyWhenMedipimSaysNotFound() {
        String body = "{\"error\": {\"status\": 404, \"code\": \"not_found\", \"message\": \"Product not found\"}}";

        assertNull(apiAnswering(404, body).searchProductById("M123"));
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 405, 500, 502, 503, 504})
    void throwsInsteadOfReportingNotFoundOnAnyOtherStatus(int status) {
        MedipimException exception = assertThrows(
                MedipimException.class,
                () -> apiAnswering(status, "<html>error</html>").searchProductById("M123")
        );

        Throwable cause = rootCause(exception);
        assertInstanceOf(UnexpectedStatusCodeException.class, cause);
        assertTrue(cause.getMessage().contains(String.valueOf(status)));
    }

    @Test
    void stillThrowsRateLimitOn429() {
        MedipimException exception = assertThrows(
                MedipimException.class,
                () -> apiAnswering(429, "{}").searchProductById("M123")
        );

        assertInstanceOf(RateLimitException.class, rootCause(exception));
    }
}
