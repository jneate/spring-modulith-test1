package dev.neate.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for Country entity.
 * 
 * Verifies that:
 * - Entity can be instantiated
 * - Getters and setters work correctly
 * - Default values are set appropriately
 * - Entity fields can be modified
 */
class CountryTest {

    @Test
    void canCreateCountryWithNoArgsConstructor() {
        // Create entity using no-args constructor
        Country country = new Country();
        
        assertThat(country)
            .as("Country should be created")
            .isNotNull();
        
        assertThat(country.getValidCountry())
            .as("validCountry should default to false")
            .isFalse();
    }

    @Test
    void canCreateCountryWithRequiredFields() {
        // Create entity with required fields
        Country country = new Country("United Kingdom", "GB");
        
        assertThat(country.getName())
            .as("Name should be set")
            .isEqualTo("United Kingdom");
        
        assertThat(country.getCode())
            .as("Code should be set")
            .isEqualTo("GB");
        
        assertThat(country.getValidCountry())
            .as("validCountry should default to false")
            .isFalse();
    }

    @Test
    void canSetAndGetAllFields() {
        // Create entity and set all fields
        Country country = new Country();
        country.setId("123456789");
        country.setName("France");
        country.setCode("FR");
        country.setCurrency("EUR");
        country.setLanguage("French");
        country.setPopulation("67000000");
        country.setValidCountry(true);
        
        // Verify all fields
        assertThat(country.getId()).isEqualTo("123456789");
        assertThat(country.getName()).isEqualTo("France");
        assertThat(country.getCode()).isEqualTo("FR");
        assertThat(country.getCurrency()).isEqualTo("EUR");
        assertThat(country.getLanguage()).isEqualTo("French");
        assertThat(country.getPopulation()).isEqualTo("67000000");
        assertThat(country.getValidCountry()).isTrue();
    }

    @Test
    void canModifyCountryFields() {
        // Create entity with initial values
        Country country = new Country("Germany", "DE");
        
        // Modify fields
        country.setCurrency("EUR");
        country.setLanguage("German");
        country.setPopulation("83000000");
        country.setValidCountry(true);
        
        // Verify modifications
        assertThat(country.getCurrency()).isEqualTo("EUR");
        assertThat(country.getLanguage()).isEqualTo("German");
        assertThat(country.getPopulation()).isEqualTo("83000000");
        assertThat(country.getValidCountry()).isTrue();
    }

    @Test
    void toStringContainsAllFields() {
        // Create entity with all fields
        Country country = new Country("Spain", "ES");
        country.setId("abc123");
        country.setCurrency("EUR");
        country.setLanguage("Spanish");
        country.setPopulation("47000000");
        country.setValidCountry(true);
        
        String toString = country.toString();
        
        // Verify toString contains all field values
        assertThat(toString)
            .contains("abc123")
            .contains("Spain")
            .contains("ES")
            .contains("EUR")
            .contains("Spanish")
            .contains("47000000")
            .contains("true");
    }
}
