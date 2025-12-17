package fr.lecomptoirdespharmacies.medipim.exceptions;

public class MedipimException extends RuntimeException {

    public MedipimException(String message) {
        super(message);
    }

    public MedipimException(Throwable cause) {
        super(cause);
    }
}
