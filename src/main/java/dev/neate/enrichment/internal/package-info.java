/**
 * Enrichment Module - Internal implementation details.
 * 
 * This package contains internal components that should not be accessed
 * directly by other modules:
 * - RestCountriesClient
 * - CountryEnrichmentService
 * - CountryValidatedEventListener
 * 
 * Spring Modulith enforces that these components remain encapsulated within
 * the enrichment module.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Enrichment Internal"
)
package dev.neate.enrichment.internal;
