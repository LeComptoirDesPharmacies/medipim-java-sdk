package fr.lecomptoirdespharmacies.medipim.exceptions;

public class RateLimitException extends UnexpectedStatusCodeException {

    public RateLimitException(String message) {
        super(message);
    }
}
