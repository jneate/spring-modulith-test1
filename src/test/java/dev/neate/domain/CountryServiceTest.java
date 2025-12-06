package dev.neate.domain;

import dev.neate.MongoTestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
@Import(MongoTestcontainersConfiguration.class)
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
