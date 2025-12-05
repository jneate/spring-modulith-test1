/**
 * Validation Module - Internal implementation details.
 * 
 * This package contains internal components that should not be accessed
 * directly by other modules:
 * - CountryValidationService
 * - CountryCreatedEventListener
 * 
 * Spring Modulith enforces that these components remain encapsulated within
 * the validation module.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Validation Internal"
)
package dev.neate.validation.internal;
