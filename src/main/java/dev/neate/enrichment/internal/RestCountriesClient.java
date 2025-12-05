package dev.neate.enrichment.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Client for calling the RestCountries API.
 * 
 * This component is internal to the Enrichment module and handles HTTP
 * communication with the external RestCountries API. It fetches country
 * data including population, currency, and language information.
 * 
 * The client uses RestClient for HTTP calls with sensible timeout and
 * connection settings.
 */
@Component
class RestCountriesClient {

    private static final Logger log = LoggerFactory.getLogger(RestCountriesClient.class);
    private static final String BASE_URL = "https://restcountries.com/v3.1";
    
    private final RestClient restClient;

    /**
     * Constructor that creates a configured RestClient.
     */
    public RestCountriesClient() {
        this.restClient = RestClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    /**
     * Constructor for dependency injection (used in tests).
     *
     * @param restClient the RestClient to use
     */
    RestCountriesClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Fetches country data from the RestCountries API.
     * 
     * Makes a GET request to /alpha/{code} endpoint and extracts:
     * - Population (number converted to string)
     * - First currency code from currencies object
     * - First language name from languages object
     *
     * @param countryCode the ISO 3166-1 alpha-2 country code (e.g., "GB")
     * @return enrichment data containing population, currency, and language
     * @throws EnrichmentException if the API call fails or response cannot be parsed
     */
    @SuppressWarnings("unchecked")
    public EnrichmentData fetchCountryData(String countryCode) throws EnrichmentException {
        log.debug("Fetching country data for code: {}", countryCode);
        
        try {
            // Call API - returns a list with one map
            List<Map<String, Object>> response = restClient.get()
                .uri("/alpha/{code}", countryCode)
                .retrieve()
                .body(List.class);
            
            if (response == null || response.isEmpty()) {
                throw new EnrichmentException("No data returned for country code: " + countryCode);
            }
            
            Map<String, Object> countryData = response.get(0);
            
            // Extract population
            String population = extractPopulation(countryData);
            
            // Extract first currency code
            String currency = extractFirstCurrency(countryData);
            
            // Extract first language name
            String language = extractFirstLanguage(countryData);
            
            log.debug("Successfully fetched data for {}: population={}, currency={}, language={}", 
                countryCode, population, currency, language);
            
            return new EnrichmentData(population, currency, language);
            
        } catch (EnrichmentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch country data for code: {}", countryCode, e);
            throw new EnrichmentException("Failed to fetch country data for code: " + countryCode, e);
        }
    }

    /**
     * Extracts population from the country data.
     */
    @SuppressWarnings("unchecked")
    private String extractPopulation(Map<String, Object> countryData) throws EnrichmentException {
        Object populationObj = countryData.get("population");
        if (populationObj == null) {
            throw new EnrichmentException("Population data not found");
        }
        return String.valueOf(populationObj);
    }

    /**
     * Extracts the first currency code from the currencies object.
     * Example: {"GBP": {"symbol": "£", "name": "British pound"}} -> "GBP"
     */
    @SuppressWarnings("unchecked")
    private String extractFirstCurrency(Map<String, Object> countryData) throws EnrichmentException {
        Object currenciesObj = countryData.get("currencies");
        if (currenciesObj == null || !(currenciesObj instanceof Map)) {
            throw new EnrichmentException("Currencies data not found");
        }
        
        Map<String, Object> currencies = (Map<String, Object>) currenciesObj;
        if (currencies.isEmpty()) {
            throw new EnrichmentException("No currency codes found");
        }
        
        return currencies.keySet().iterator().next();
    }

    /**
     * Extracts the first language name from the languages object.
     * Example: {"eng": "English"} -> "English"
     */
    @SuppressWarnings("unchecked")
    private String extractFirstLanguage(Map<String, Object> countryData) throws EnrichmentException {
        Object languagesObj = countryData.get("languages");
        if (languagesObj == null || !(languagesObj instanceof Map)) {
            throw new EnrichmentException("Languages data not found");
        }
        
        Map<String, Object> languages = (Map<String, Object>) languagesObj;
        if (languages.isEmpty()) {
            throw new EnrichmentException("No languages found");
        }
        
        return String.valueOf(languages.values().iterator().next());
    }
}
