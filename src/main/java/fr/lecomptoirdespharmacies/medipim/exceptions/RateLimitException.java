package fr.lecomptoirdespharmacies.medipim.exceptions;

public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }
}
