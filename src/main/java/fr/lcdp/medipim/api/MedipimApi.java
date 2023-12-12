package fr.lcdp.medipim.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.lcdp.medipim.api.client.Client;
import fr.lcdp.medipim.api.client.Request;
import fr.lcdp.medipim.api.client.Response;
import org.openapitools.jackson.nullable.JsonNullableModule;


import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;


public abstract class MedipimApi {

    private final Client ws;

    private final ObjectMapper objectMapper;

    private final String baseUrl;

    private final String username;

    private final String password;

    public MedipimApi(Client ws,
                      String baseUrl,
                      String username,
                      String password) {
        this.ws = ws;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.registerModule(new JsonNullableModule()); // Allow management of 'nullable: true' on schema field
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // Do not serialize 'null'. If you want null capability, set 'nullable: true' on your field in yaml
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
    }

    protected Request createAuthenticatedRequest(String endpoint) {
        return ws.url(baseUrl + endpoint).
                setAuth(this.username, this.password);
    }

    protected <T> T deserialize(Object fromValue, Class<T> toValueType) {
        return objectMapper.convertValue(fromValue, toValueType);
    }

    protected <T> List<T> deserializeList(Object fromValue, Class<T> toValueType) {
        JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, toValueType);

        return objectMapper.convertValue(fromValue, listType);
    }

    protected <T extends JsonNode> T serialize(Object fromValue) {
        return objectMapper.valueToTree(fromValue);
    }

    protected Stream<JsonNode> readStream(Response response) {
        return Arrays.stream(response.getBody().split("\n"))
                .map(responsePart -> {
                    try {
                        return objectMapper.readTree(responsePart);
                    } catch (IOException e) {
                        throw new RuntimeException("Error parsing JSON from WS response wsBody", e);
                    }
                });
    }

}