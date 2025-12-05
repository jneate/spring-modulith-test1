package dev.neate.domain;

import dev.neate.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for CountryService.
 * 
 * Verifies that:
 * - Service operations work correctly
 * - Service properly delegates to repository
 * - Validation logic is applied
 * - Edge cases are handled appropriately
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
    "spring.data.mongodb.database=test-country-service-db"
})
class CountryServiceTest {

    @Autowired
    private CountryService countryService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        mongoTemplate.getDb().drop();
    }

    @Test
    void canSaveCountry() {
        // Create and save a country
        Country country = new Country("United Kingdom", "GB");
        country.setCurrency("GBP");
        
        Country saved = countryService.save(country);
        
        // Verify the country was saved
        assertThat(saved.getId())
            .as("ID should be generated")
            .isNotNull();
        
        assertThat(saved.getName()).isEqualTo("United Kingdom");
        assertThat(saved.getCode()).isEqualTo("GB");
        assertThat(saved.getCurrency()).isEqualTo("GBP");
    }

    @Test
    void canFindCountryById() {
        // Save a country
        Country country = new Country("France", "FR");
        Country saved = countryService.save(country);
        
        // Find by ID
        Optional<Country> found = countryService.findById(saved.getId());
        
        // Verify the country was found
        assertThat(found)
            .as("Country should be found")
            .isPresent();
        
        assertThat(found.get().getName()).isEqualTo("France");
        assertThat(found.get().getCode()).isEqualTo("FR");
    }

    @Test
    void findByIdReturnsEmptyForNonExistentId() {
        // Try to find a non-existent country
        Optional<Country> found = countryService.findById(UUID.randomUUID());
        
        assertThat(found)
            .as("Should return empty for non-existent ID")
            .isEmpty();
    }

    @Test
    void canUpdateExistingCountry() {
        // Save a country
        Country country = new Country("Germany", "DE");
        Country saved = countryService.save(country);
        
        // Update the country
        saved.setCurrency("EUR");
        saved.setLanguage("German");
        saved.setPopulation("83000000");
        saved.setValidCountry(true);
        
        Country updated = countryService.update(saved);
        
        // Verify the update
        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getCurrency()).isEqualTo("EUR");
        assertThat(updated.getLanguage()).isEqualTo("German");
        assertThat(updated.getPopulation()).isEqualTo("83000000");
        assertThat(updated.getValidCountry()).isTrue();
    }

    @Test
    void updateThrowsExceptionForNullId() {
        // Create a country without ID
        Country country = new Country("Spain", "ES");
        
        // Try to update without ID
        assertThatThrownBy(() -> countryService.update(country))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Country ID cannot be null");
    }

    @Test
    void updateThrowsExceptionForNonExistentCountry() {
        // Create a country with a non-existent ID
        Country country = new Country("Italy", "IT");
        country.setId(UUID.randomUUID());
        
        // Try to update non-existent country
        assertThatThrownBy(() -> countryService.update(country))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("does not exist");
    }

    @Test
    void canSaveMultipleCountries() {
        // Save multiple countries
        Country uk = countryService.save(new Country("United Kingdom", "GB"));
        Country fr = countryService.save(new Country("France", "FR"));
        Country de = countryService.save(new Country("Germany", "DE"));
        
        // Verify all were saved with unique IDs
        assertThat(uk.getId()).isNotNull();
        assertThat(fr.getId()).isNotNull();
        assertThat(de.getId()).isNotNull();
        
        assertThat(uk.getId()).isNotEqualTo(fr.getId());
        assertThat(fr.getId()).isNotEqualTo(de.getId());
    }

    @Test
    void saveCanUpdateExistingCountry() {
        // Save a country
        Country country = new Country("Portugal", "PT");
        Country saved = countryService.save(country);
        
        // Modify and save again (update)
        saved.setCurrency("EUR");
        Country updated = countryService.save(saved);
        
        // Verify it's the same entity (same ID)
        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getCurrency()).isEqualTo("EUR");
        
        // Verify only one entity exists
        Optional<Country> found = countryService.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCurrency()).isEqualTo("EUR");
    }
}
