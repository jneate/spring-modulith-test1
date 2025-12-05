package dev.neate.enrichment.internal;

import dev.neate.domain.Country;
import dev.neate.domain.CountryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for enriching country data with external API information.
 * 
 * This service orchestrates the enrichment process by:
 * 1. Fetching data from the RestCountries API
 * 2. Updating the country entity with enrichment data
 * 3. Saving the enriched country via the domain service
 * 
 * This component is internal to the Enrichment module and not exposed
 * to other modules.
 */
@Service
class CountryEnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(CountryEnrichmentService.class);

    private final RestCountriesClient restCountriesClient;
    private final CountryService countryService;

    /**
     * Constructor with dependency injection.
     *
     * @param restCountriesClient the REST client for fetching country data
     * @param countryService the domain service for saving countries
     */
    public CountryEnrichmentService(
            RestCountriesClient restCountriesClient,
            CountryService countryService) {
        this.restCountriesClient = restCountriesClient;
        this.countryService = countryService;
    }

    /**
     * Enriches a country with data from the external API.
     * 
     * This method:
     * 1. Fetches enrichment data using the country code
     * 2. Updates the country entity with population, currency, and language
     * 3. Saves the enriched country
     * 
     * @param country the country to enrich
     * @throws EnrichmentException if the enrichment process fails
     */
    public void enrichCountry(Country country) throws EnrichmentException {
        log.debug("Enriching country: {} ({})", country.getName(), country.getCode());

        // Fetch enrichment data from API
        EnrichmentData data = restCountriesClient.fetchCountryData(country.getCode());

        // Update country entity
        country.setPopulation(data.population());
        country.setCurrency(data.currency());
        country.setLanguage(data.language());

        // Save enriched country
        countryService.save(country);

        log.info("Successfully enriched country: {} with population={}, currency={}, language={}",
            country.getName(), data.population(), data.currency(), data.language());
    }
}
