package dev.neate.api;

import dev.neate.domain.Country;
import dev.neate.domain.CountryService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

/**
 * REST controller for country operations.
 * 
 * This controller provides the HTTP API for creating countries.
 * It delegates business logic to the CountryService and publishes
 * events for other modules to react to.
 */
@RestController
@RequestMapping("/countries")
public class CountryController {

    private final CountryService countryService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructor injection of dependencies.
     *
     * @param countryService the country service from the domain module
     * @param eventPublisher Spring's event publisher for publishing domain events
     */
    public CountryController(CountryService countryService, ApplicationEventPublisher eventPublisher) {
        this.countryService = countryService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Create a new country.
     * 
     * This endpoint:
     * 1. Receives a CreateCountryRequest with name and code
     * 2. Creates a Country entity with validCountry=false
     * 3. Saves the entity (MongoDB generates the ID)
     * 4. Publishes a CountryCreatedEvent with the generated ID
     * 5. Returns 202 Accepted (no body)
     *
     * @param request the country creation request
     * @return 202 Accepted response
     */
    @PostMapping
    @Transactional // Important otherwise events won't be consumed
    public ResponseEntity<Void> createCountry(@RequestBody CreateCountryRequest request) {
        // Create country entity
        Country country = new Country(request.name(), request.code());
        
        // Save country (MongoDB generates ID)
        Country savedCountry = countryService.save(country);
        
        // Publish event for other modules to react
        eventPublisher.publishEvent(new CountryCreatedEvent(savedCountry.getId()));
        
        // Return 202 Accepted
        return ResponseEntity.accepted().build();
    }
}
