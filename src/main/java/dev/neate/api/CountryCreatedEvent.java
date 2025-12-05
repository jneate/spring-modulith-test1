package dev.neate.api;

import java.util.UUID;

/**
 * Spring Modulith event published when a country is created.
 * 
 * This event is published by the API module after successfully saving
 * a new country to the database. Other modules can listen to this event
 * to perform additional processing (e.g., validation, enrichment).
 * 
 * The event is immutable and contains only the country ID, allowing
 * listeners to fetch the full country details if needed.
 *
 * @param countryId the ID of the created country (UUID)
 */
public record CountryCreatedEvent(UUID countryId) {
}
