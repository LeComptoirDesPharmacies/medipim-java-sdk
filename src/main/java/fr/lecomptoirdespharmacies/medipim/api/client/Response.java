package fr.lecomptoirdespharmacies.medipim.api.client;

import com.fasterxml.jackson.databind.JsonNode;

public interface Response {
    /**
     * @return the HTTP status code from the response.
     */
    int getStatus();

    /** @return the body as a string. */
    String getBody();

    /**
     * Gets the body as JSON node.
     *
     * @return json node.
     */
    JsonNode asJson();
}
