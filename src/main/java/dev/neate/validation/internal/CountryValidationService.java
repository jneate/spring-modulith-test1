package dev.neate.validation.internal;

import dev.neate.domain.Country;
import org.springframework.stereotype.Service;

/**
 * Service for validating country data.
 * 
 * This service is internal to the Validation module and not exposed
 * to other modules. It encapsulates the validation logic for countries.
 */
@Service
class CountryValidationService {

    /**
     * Validates a country based on business rules.
     * 
     * Validation rules:
     * - Name must not be null or empty (after trim)
     * - Code must not be null or empty (after trim)
     * 
     * Note: This does not validate currency, language, or population fields.
     *
     * @param country the country to validate
     * @return true if all validation checks pass, false otherwise
     */
    public boolean validate(Country country) {
        if (country == null) {
            return false;
        }

        // Check name is not null and not empty (after trim)
        if (country.getName() == null || country.getName().trim().isEmpty()) {
            return false;
        }

        // Check code is not null and not empty (after trim)
        if (country.getCode() == null || country.getCode().trim().isEmpty()) {
            return false;
        }

        // All checks passed
        return true;
    }
}
