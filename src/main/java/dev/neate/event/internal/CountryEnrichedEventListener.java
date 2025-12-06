package dev.neate.event.internal;

import dev.neate.domain.Country;
import dev.neate.domain.CountryService;
import dev.neate.enrichment.CountryEnrichedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Event listener for CountryEnrichedEvent.
 * 
 * This listener responds to country enrichment events from the Enrichment module,
 * fetches the enriched country data, and publishes it to Kafka.
 * 
 * The listener includes retry support via Spring Modulith's @ApplicationModuleListener
 * annotation, which will automatically retry on failure up to 3 times with exponential
 * backoff.
 * 
 * This component is internal to the Event module and not exposed to other modules.
 */
@Component
class CountryEnrichedEventListener {

    private static final Logger log = LoggerFactory.getLogger(CountryEnrichedEventListener.class);

    private final CountryService countryService;
    private final CountryKafkaProducer kafkaProducer;

    /**
     * Constructor injection of dependencies.
     *
     * @param countryService the country service from the domain module
     * @param kafkaProducer the Kafka producer for sending country events
     */
    public CountryEnrichedEventListener(
            CountryService countryService,
            CountryKafkaProducer kafkaProducer) {
        this.countryService = countryService;
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * Handles CountryEnrichedEvent by sending country data to Kafka.
     * 
     * Process:
     * 1. Extract country ID from event
     * 2. Fetch country from domain using CountryService.findById()
     * 3. If country not found, log error and return
     * 4. Call CountryKafkaProducer.sendCountryEvent(country)
     * 5. If successful, log success
     * 6. If exception thrown, let Spring Modulith retry mechanism handle it
     * 
     * Retry configuration:
     * - Maximum attempts: 3
     * - Backoff: Exponential (handled by Spring Modulith)
     *
     * @param event the country enriched event
     * @throws org.springframework.kafka.KafkaException if Kafka send fails (triggers retry)
     */
    @ApplicationModuleListener
    public void handleCountryEnriched(CountryEnrichedEvent event) {
        log.debug("Received CountryEnrichedEvent for country ID: {}", event.countryId());

        // Fetch country from domain
        Optional<Country> optionalCountry = countryService.findById(event.countryId());

        if (optionalCountry.isEmpty()) {
            log.error("Country not found with ID: {}", event.countryId());
            return;
        }

        Country country = optionalCountry.get();

        // Send to Kafka
        log.info("Sending enriched country to Kafka: {} ({})", country.getName(), country.getCode());
        kafkaProducer.sendCountryEvent(country);

        log.info("Successfully sent enriched country to Kafka: {} ({})", 
            country.getName(), country.getCode());
    }
}
