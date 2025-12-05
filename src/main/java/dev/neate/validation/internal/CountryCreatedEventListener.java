package dev.neate.validation.internal;

import dev.neate.api.CountryCreatedEvent;
import dev.neate.domain.Country;
import dev.neate.domain.CountryService;
import dev.neate.validation.CountryValidatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Event listener for CountryCreatedEvent.
 * 
 * This listener responds to country creation events from the API module,
 * validates the country, and publishes a CountryValidatedEvent if validation passes.
 * 
 * This component is internal to the Validation module and not exposed to other modules.
 */
@Component
class CountryCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(CountryCreatedEventListener.class);

    private final CountryService countryService;
    private final CountryValidationService validationService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructor injection of dependencies.
     *
     * @param countryService the country service from the domain module
     * @param validationService the validation service
     * @param eventPublisher Spring's event publisher for publishing domain events
     */
    public CountryCreatedEventListener(
            CountryService countryService,
            CountryValidationService validationService,
            ApplicationEventPublisher eventPublisher) {
        this.countryService = countryService;
        this.validationService = validationService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handles CountryCreatedEvent by validating the country.
     * 
     * Process:
     * 1. Extract country ID from event
     * 2. Fetch country from domain using CountryService.findById()
     * 3. If country not found, log error and return
     * 4. Call CountryValidationService.validate(country)
     * 5. If validation passes:
     *    - Set country.validCountry = true
     *    - Save country via CountryService.save()
     *    - Publish CountryValidatedEvent with country ID
     * 6. If validation fails:
     *    - Do NOT save (entity remains with validCountry=false)
     *    - Log validation failure
     *    - Do NOT publish event
     *
     * @param event the country created event
     */
    @ApplicationModuleListener
    public void handleCountryCreated(CountryCreatedEvent event) {
        log.debug("Received CountryCreatedEvent for country ID: {}", event.countryId());

        // Extract country ID from event
        UUID countryId = event.countryId();

        // Fetch country from domain
        Optional<Country> optionalCountry = countryService.findById(countryId);

        if (optionalCountry.isEmpty()) {
            log.error("Country not found with ID: {}", countryId);
            return;
        }

        Country country = optionalCountry.get();

        // Validate the country
        boolean isValid = validationService.validate(country);

        if (isValid) {
            // Set validCountry flag to true
            country.setValidCountry(true);

            // Save the updated country
            countryService.save(country);

            log.info("Country validated successfully: {} ({})", country.getName(), country.getCode());

            // Publish CountryValidatedEvent
            eventPublisher.publishEvent(new CountryValidatedEvent(countryId));
        } else {
            // Validation failed - do not save, do not publish event
            log.warn("Country validation failed for ID: {} - name: {}, code: {}",
                    countryId, country.getName(), country.getCode());
        }
    }
}
