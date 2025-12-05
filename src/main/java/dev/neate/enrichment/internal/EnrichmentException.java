package dev.neate.enrichment.internal;

/**
 * Exception thrown when country enrichment fails.
 * 
 * This exception is used to signal failures during the enrichment process,
 * such as API call failures, parsing errors, or network issues.
 * Spring Modulith's retry mechanism will handle this exception.
 */
class EnrichmentException extends Exception {

    /**
     * Constructs a new EnrichmentException with the specified detail message.
     *
     * @param message the detail message
     */
    public EnrichmentException(String message) {
        super(message);
    }

    /**
     * Constructs a new EnrichmentException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public EnrichmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
