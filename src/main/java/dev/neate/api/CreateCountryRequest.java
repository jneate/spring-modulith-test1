package dev.neate.api;

/**
 * Request DTO for creating a new country.
 * 
 * This record is used as the request body for the POST /countries endpoint.
 * It contains the minimal required information to create a country entity.
 * 
 * The DTO is immutable and will be automatically deserialized from JSON
 * by Spring's Jackson integration.
 * 
 * Example JSON:
 * <pre>
 * {
 *   "name": "United Kingdom",
 *   "code": "GB"
 * }
 * </pre>
 *
 * @param name the country name (e.g., "United Kingdom")
 * @param code the ISO 3166-1 alpha-2 country code (e.g., "GB")
 */
public record CreateCountryRequest(
    String name,
    String code
) {
}
