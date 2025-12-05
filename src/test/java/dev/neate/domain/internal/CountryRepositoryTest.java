package dev.neate.domain.internal;

import dev.neate.TestcontainersConfiguration;
import dev.neate.domain.Country;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for CountryRepository.
 * 
 * Verifies that:
 * - Repository can perform CRUD operations
 * - MongoDB integration works correctly
 * - Entities can be saved and retrieved
 * - Repository methods work as expected
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
    "spring.data.mongodb.database=test-country-repo-db"
})
class CountryRepositoryTest {

    @Autowired
    private CountryRepository repository;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        repository.deleteAll();
    }

    @Test
    void canSaveCountry() {
        // Create and save a country
        Country country = new Country("United Kingdom", "GB");
        country.setCurrency("GBP");
        country.setLanguage("English");
        country.setPopulation("67000000");
        
        Country saved = repository.save(country);
        
        // Verify the country was saved
        assertThat(saved.getId())
            .as("ID should be generated")
            .isNotNull();
        
        assertThat(saved.getName()).isEqualTo("United Kingdom");
        assertThat(saved.getCode()).isEqualTo("GB");
    }

    @Test
    void canFindCountryById() {
        // Save a country
        Country country = new Country("France", "FR");
        Country saved = repository.save(country);
        
        // Find by ID
        Optional<Country> found = repository.findById(saved.getId());
        
        // Verify the country was found
        assertThat(found)
            .as("Country should be found")
            .isPresent();
        
        assertThat(found.get().getName()).isEqualTo("France");
        assertThat(found.get().getCode()).isEqualTo("FR");
    }

    @Test
    void canUpdateCountry() {
        // Save a country
        Country country = new Country("Germany", "DE");
        Country saved = repository.save(country);
        
        // Update the country
        saved.setCurrency("EUR");
        saved.setLanguage("German");
        saved.setValidCountry(true);
        Country updated = repository.save(saved);
        
        // Verify the update
        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getCurrency()).isEqualTo("EUR");
        assertThat(updated.getLanguage()).isEqualTo("German");
        assertThat(updated.getValidCountry()).isTrue();
    }

    @Test
    void canDeleteCountry() {
        // Save a country
        Country country = new Country("Spain", "ES");
        Country saved = repository.save(country);
        
        // Delete the country
        repository.delete(saved);
        
        // Verify deletion
        Optional<Country> found = repository.findById(saved.getId());
        assertThat(found)
            .as("Country should not be found after deletion")
            .isEmpty();
    }

    @Test
    void findByIdReturnsEmptyForNonExistentId() {
        // Try to find a non-existent country
        Optional<Country> found = repository.findById("nonexistent-id");
        
        assertThat(found)
            .as("Should return empty for non-existent ID")
            .isEmpty();
    }

    @Test
    void canCountCountries() {
        // Save multiple countries
        repository.save(new Country("Italy", "IT"));
        repository.save(new Country("Portugal", "PT"));
        repository.save(new Country("Greece", "GR"));
        
        // Count countries
        long count = repository.count();
        
        assertThat(count)
            .as("Should have 3 countries")
            .isEqualTo(3);
    }
}
