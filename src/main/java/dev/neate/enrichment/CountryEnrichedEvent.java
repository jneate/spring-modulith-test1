package dev.neate.enrichment;

import java.util.UUID;

/**
 * Spring Modulith event published when a country has been enriched.
 * 
 * This event is published by the Enrichment module after successfully
 * enriching a country with data from the RestCountries API. Other modules
 * can listen to this event to perform further processing.
 * 
 * @param countryId the ID of the enriched country (UUID)
 */
public record CountryEnrichedEvent(UUID countryId) {
    
    /**
     * Creates a new CountryEnrichedEvent.
     *
     * @param countryId the ID of the enriched country (UUID)
     * @throws IllegalArgumentException if countryId is null
     */
    public CountryEnrichedEvent {
        if (countryId == null) {
            throw new IllegalArgumentException("Country ID must not be null");
        }
    }
}
