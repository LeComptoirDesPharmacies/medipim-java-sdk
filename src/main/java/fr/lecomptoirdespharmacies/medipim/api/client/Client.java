package fr.lecomptoirdespharmacies.medipim.api.client;

public interface Client {

    /**
     * Returns a Request object representing the URL. You can append additional properties on the
     * WSRequest by chaining calls, and execute the request to return an asynchronous {@code
     * Promise<Response>}.
     *
     * @param url the URL to request
     * @return the request
     */
    Request url(String url);

}
