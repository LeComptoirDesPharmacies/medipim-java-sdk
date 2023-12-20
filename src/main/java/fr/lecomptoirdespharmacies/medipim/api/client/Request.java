package fr.lecomptoirdespharmacies.medipim.api.client;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.concurrent.CompletionStage;

public interface Request {
    /**
     * Sets the authentication header for the current request using BASIC authentication.
     *
     * @param username the basic auth username
     * @param password the basic auth password
     * @return the modified WSRequest.
     */
    Request setAuth(String username, String password);

    /**
     * Sets a query parameter with the given name, this can be called repeatedly. Duplicate query
     * parameters are allowed.
     *
     * @param name the query parameter name
     * @param value the query parameter value
     * @return the modified Request.
     */
    Request addQueryParameter(String name, String value);

    /**
     * Perform a GET on the request asynchronously.
     *
     * @return a promise to the response
     */

    CompletionStage<Response> get();

    /**
     * Perform a POST on the request asynchronously.
     *
     * @param body represented as JSON
     * @return a promise to the response
     */
    CompletionStage<Response> post(JsonNode body);
}
