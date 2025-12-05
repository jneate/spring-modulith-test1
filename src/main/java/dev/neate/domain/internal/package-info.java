/**
 * Domain Module - Internal implementation details.
 * 
 * This package contains internal components that should not be accessed
 * directly by other modules:
 * - CountryRepository
 * - CountryServiceImpl
 * 
 * Spring Modulith enforces that these components remain encapsulated within
 * the domain module.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Domain Internal"
)
package dev.neate.domain.internal;
