package dev.neate.enrichment.internal;

/**
 * Data transfer object for country enrichment data from RestCountries API.
 * 
 * This record encapsulates the enrichment data fetched from the external API.
 * All fields are stored as strings for simplicity.
 * 
 * @param population the country's population as a string
 * @param currency the currency code (e.g., "GBP")
 * @param language the language name (e.g., "English")
 */
record EnrichmentData(
    String population,
    String currency,
    String language
) {
    /**
     * Creates a new EnrichmentData record.
     *
     * @param population the country's population
     * @param currency the currency code
     * @param language the language name
     */
    EnrichmentData {
        // Compact constructor for validation if needed
    }
}
