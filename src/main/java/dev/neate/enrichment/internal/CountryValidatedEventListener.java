package dev.neate.enrichment.internal;

import dev.neate.domain.Country;
import dev.neate.domain.CountryService;
import dev.neate.enrichment.CountryEnrichedEvent;
import dev.neate.validation.CountryValidatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Event listener for CountryValidatedEvent.
 * 
 * This listener responds to country validation events from the Validation module,
 * enriches valid countries with external API data, and publishes a CountryEnrichedEvent
 * if enrichment succeeds.
 * 
 * The listener includes retry support via Spring Modulith's @ApplicationModuleListener
 * annotation, which will automatically retry on failure up to 3 times with exponential
 * backoff.
 * 
 * This component is internal to the Enrichment module and not exposed to other modules.
 */
@Component
class CountryValidatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(CountryValidatedEventListener.class);

    private final CountryService countryService;
    private final CountryEnrichmentService enrichmentService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructor injection of dependencies.
     *
     * @param countryService the country service from the domain module
     * @param enrichmentService the enrichment service
     * @param eventPublisher Spring's event publisher for publishing domain events
     */
    public CountryValidatedEventListener(
            CountryService countryService,
            CountryEnrichmentService enrichmentService,
            ApplicationEventPublisher eventPublisher) {
        this.countryService = countryService;
        this.enrichmentService = enrichmentService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handles CountryValidatedEvent by enriching valid countries.
     * 
     * Process:
     * 1. Extract country ID from event
     * 2. Fetch country from domain using CountryService.findById()
     * 3. If country not found, log error and return
     * 4. Check if country.validCountry == true
     * 5. If not valid, log warning and return (don't enrich invalid countries)
     * 6. Call CountryEnrichmentService.enrichCountry(country)
     * 7. If successful, publish CountryEnrichedEvent with country ID
     * 8. If exception thrown, let Spring Modulith retry mechanism handle it
     * 
     * Retry configuration:
     * - Maximum attempts: 3
     * - Backoff: Exponential (handled by Spring Modulith)
     *
     * @param event the country validated event
     * @throws EnrichmentException if enrichment fails (triggers retry)
     */
    @ApplicationModuleListener
    public void handleCountryValidated(CountryValidatedEvent event) throws EnrichmentException {
        log.debug("Received CountryValidatedEvent for country ID: {}", event.countryId());

        // Fetch country from domain
        Optional<Country> optionalCountry = countryService.findById(event.countryId());

        if (optionalCountry.isEmpty()) {
            log.error("Country not found with ID: {}", event.countryId());
            return;
        }

        Country country = optionalCountry.get();

        // Check if country is valid
        if (!country.getValidCountry()) {
            log.warn("Country {} ({}) is not valid, skipping enrichment", 
                country.getName(), country.getCode());
            return;
        }

        // Enrich the country
        log.info("Enriching valid country: {} ({})", country.getName(), country.getCode());
        enrichmentService.enrichCountry(country);

        // Publish CountryEnrichedEvent
        CountryEnrichedEvent enrichedEvent = new CountryEnrichedEvent(country.getId());
        eventPublisher.publishEvent(enrichedEvent);
        
        log.info("Published CountryEnrichedEvent for country: {} ({})", 
            country.getName(), country.getCode());
    }
}
