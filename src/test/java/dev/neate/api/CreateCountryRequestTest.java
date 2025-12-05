package dev.neate.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for CreateCountryRequest.
 * 
 * Verifies that:
 * - DTO can be instantiated
 * - DTO is immutable (record)
 * - DTO fields are accessible
 * - DTO equality works correctly
 * 
 * Note: JSON serialization/deserialization will be tested in the controller integration tests.
 */
class CreateCountryRequestTest {

    @Test
    void canCreateRequest() {
        // Create request
        CreateCountryRequest request = new CreateCountryRequest("United Kingdom", "GB");
        
        assertThat(request)
            .as("Request should be created")
            .isNotNull();
        
        assertThat(request.name())
            .as("Name should be set")
            .isEqualTo("United Kingdom");
        
        assertThat(request.code())
            .as("Code should be set")
            .isEqualTo("GB");
    }

    @Test
    void requestIsImmutable() {
        // Create request
        CreateCountryRequest request = new CreateCountryRequest("France", "FR");
        
        // Verify it's a record (immutable)
        assertThat(request.getClass().isRecord())
            .as("Request should be a record")
            .isTrue();
    }

    @Test
    void requestsWithSameDataAreEqual() {
        // Create two requests with same data
        CreateCountryRequest request1 = new CreateCountryRequest("Italy", "IT");
        CreateCountryRequest request2 = new CreateCountryRequest("Italy", "IT");
        
        assertThat(request1)
            .as("Requests with same data should be equal")
            .isEqualTo(request2);
        
        assertThat(request1.hashCode())
            .as("Hash codes should be equal")
            .isEqualTo(request2.hashCode());
    }

    @Test
    void requestsWithDifferentDataAreNotEqual() {
        // Create two requests with different data
        CreateCountryRequest request1 = new CreateCountryRequest("Portugal", "PT");
        CreateCountryRequest request2 = new CreateCountryRequest("Greece", "GR");
        
        assertThat(request1)
            .as("Requests with different data should not be equal")
            .isNotEqualTo(request2);
    }

    @Test
    void toStringContainsFields() {
        // Create request
        CreateCountryRequest request = new CreateCountryRequest("Belgium", "BE");
        
        String toString = request.toString();
        
        assertThat(toString)
            .as("toString should contain fields")
            .contains("Belgium")
            .contains("BE");
    }
}
