package dev.neate.enrichment.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Jackson DTO for RestCountries API response.
 * 
 * This class represents the JSON structure returned by the RestCountries API
 * when querying a single country by code. The API returns a list containing
 * one country object, so this represents a single country entry.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record RestCountriesResponse(
    long population,
    Map<String, CurrencyInfo> currencies,
    Map<String, String> languages
) {
    
    /**
     * Extracts the first currency code from the currencies map.
     * 
     * @return the first currency code (e.g., "GBP") or null if no currencies
     */
    String getFirstCurrencyCode() {
        return currencies != null && !currencies.isEmpty() 
            ? currencies.keySet().iterator().next() 
            : null;
    }
    
    /**
     * Extracts the first language name from the languages map.
     * 
     * @return the first language name (e.g., "English") or null if no languages
     */
    String getFirstLanguageName() {
        return languages != null && !languages.isEmpty() 
            ? languages.values().iterator().next() 
            : null;
    }
    
    /**
     * Converts this response to EnrichmentData.
     * 
     * @return EnrichmentData with extracted information
     * @throws IllegalArgumentException if required data is missing
     */
    EnrichmentData toEnrichmentData() {
        String populationStr = String.valueOf(population);
        String currency = getFirstCurrencyCode();
        String language = getFirstLanguageName();
        
        if (population == 0) {
            throw new IllegalArgumentException("Population data is missing or zero");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("No currency data found");
        }
        if (language == null || language.trim().isEmpty()) {
            throw new IllegalArgumentException("No language data found");
        }
        
        return new EnrichmentData(populationStr, currency, language);
    }
}

/**
 * DTO for currency information in RestCountries response.
 * 
 * Example JSON: {"symbol": "£", "name": "British pound"}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record CurrencyInfo(
    String symbol,
    String name
) {}
