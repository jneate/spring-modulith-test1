/**
 * Enrichment Module - External API data enrichment.
 * 
 * Public API:
 * - CountryEnrichedEvent (event only)
 * 
 * Internal (not exposed):
 * - RestCountriesClient
 * - CountryEnrichmentService
 * - CountryValidatedEventListener
 * 
 * This module enriches country data by calling external APIs and publishes
 * enrichment events for downstream processing.
 */
package dev.neate.enrichment;
