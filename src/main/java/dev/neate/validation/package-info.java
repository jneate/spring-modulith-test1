/**
 * Validation Module - Business rule validation.
 * 
 * Public API:
 * - CountryValidatedEvent (event only)
 * 
 * Internal (not exposed):
 * - CountryValidationService
 * - CountryCreatedEventListener
 * 
 * This module validates country data against business rules and publishes
 * validation events for downstream processing.
 */
package dev.neate.validation;
