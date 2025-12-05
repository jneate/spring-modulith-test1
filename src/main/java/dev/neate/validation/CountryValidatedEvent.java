package dev.neate.validation;

import java.util.UUID;

/**
 * Spring Modulith event published when a country has been validated.
 * 
 * This event is published by the Validation module after successfully
 * validating a country. Other modules can listen to this event to perform
 * further processing (e.g., enrichment).
 * 
 * @param countryId the ID of the validated country (UUID)
 */
public record CountryValidatedEvent(UUID countryId) {
    
    /**
     * Creates a new CountryValidatedEvent.
     *
     * @param countryId the ID of the validated country (UUID)
     * @throws IllegalArgumentException if countryId is null
     */
    public CountryValidatedEvent {
        if (countryId == null) {
            throw new IllegalArgumentException("Country ID must not be null");
        }
    }
}
