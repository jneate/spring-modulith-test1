package dev.neate.validation.internal;

import dev.neate.domain.Country;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for CountryValidationService.
 * 
 * Verifies that:
 * - Valid countries pass validation
 * - Countries with null/empty name fail validation
 * - Countries with null/empty code fail validation
 * - Countries with whitespace-only fields fail validation
 * - Null country fails validation
 * - Currency, language, and population are not validated
 */
class CountryValidationServiceTest {

    private CountryValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new CountryValidationService();
    }

    @Test
    void validCountryPassesValidation() {
        // Given
        Country country = new Country("United Kingdom", "GB");

        // When
        boolean result = validationService.validate(country);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void countryWithNullNameFailsValidation() {
        // Given
        Country country = new Country(null, "GB");

        // When
        boolean result = validationService.validate(country);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void countryWithEmptyNameFailsValidation() {
        // Given
        Country country = new Country("", "GB");

        // When
        boolean result = validationService.validate(country);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void countryWithWhitespaceOnlyNameFailsValidation() {
        // Given
        Country country = new Country("   ", "GB");

        // When
        boolean result = validationService.validate(country);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void countryWithNullCodeFailsValidation() {
        // Given
        Country country = new Country("United Kingdom", null);

        // When
        boolean result = validationService.validate(country);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void countryWithEmptyCodeFailsValidation() {
        // Given
        Country country = new Country("United Kingdom", "");

        // When
        boolean result = validationService.validate(country);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void countryWithWhitespaceOnlyCodeFailsValidation() {
        // Given
        Country country = new Country("United Kingdom", "   ");

        // When
        boolean result = validationService.validate(country);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void nullCountryFailsValidation() {
        // When
        boolean result = validationService.validate(null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void countryWithBothNullFieldsFailsValidation() {
        // Given
        Country country = new Country(null, null);

        // When
        boolean result = validationService.validate(country);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void countryWithValidNameAndCodeButNullCurrencyPassesValidation() {
        // Given - currency is not validated
        Country country = new Country("France", "FR");
        country.setCurrency(null);

        // When
        boolean result = validationService.validate(country);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void countryWithValidNameAndCodeButNullLanguagePassesValidation() {
        // Given - language is not validated
        Country country = new Country("Germany", "DE");
        country.setLanguage(null);

        // When
        boolean result = validationService.validate(country);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void countryWithValidNameAndCodeButNullPopulationPassesValidation() {
        // Given - population is not validated
        Country country = new Country("Spain", "ES");
        country.setPopulation(null);

        // When
        boolean result = validationService.validate(country);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void countryWithNameContainingWhitespacePassesValidation() {
        // Given - whitespace within the name is allowed
        Country country = new Country("United Kingdom", "GB");

        // When
        boolean result = validationService.validate(country);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void countryWithNameHavingLeadingAndTrailingWhitespacePassesValidation() {
        // Given - trim is applied, so leading/trailing whitespace is ignored
        Country country = new Country("  France  ", "FR");

        // When
        boolean result = validationService.validate(country);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void countryWithCodeHavingLeadingAndTrailingWhitespacePassesValidation() {
        // Given - trim is applied, so leading/trailing whitespace is ignored
        Country country = new Country("Germany", "  DE  ");

        // When
        boolean result = validationService.validate(country);

        // Then
        assertThat(result).isTrue();
    }
}
