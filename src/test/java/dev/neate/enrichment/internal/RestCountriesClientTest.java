package dev.neate.enrichment.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for RestCountriesClient.
 * 
 * Verifies that:
 * - Client can fetch and parse country data
 * - Population is correctly extracted
 * - First currency code is extracted
 * - First language name is extracted
 * - Errors are properly handled
 */
class RestCountriesClientTest {

    private RestClient mockRestClient;
    private RestClient.RequestHeadersUriSpec mockUriSpec;
    private RestClient.RequestHeadersSpec mockHeadersSpec;
    private RestClient.ResponseSpec mockResponseSpec;
    private RestCountriesClient client;

    @BeforeEach
    void setUp() {
        mockRestClient = mock(RestClient.class);
        mockUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        mockHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        mockResponseSpec = mock(RestClient.ResponseSpec.class);
        
        client = new RestCountriesClient(mockRestClient);
        
        // Setup mock chain
        when(mockRestClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri(anyString(), any(Object[].class))).thenReturn(mockHeadersSpec);
        when(mockHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
    }

    @Test
    void canFetchCountryData() throws Exception {
        // Given
        Map<String, Object> countryData = new HashMap<>();
        countryData.put("population", 67000000);
        countryData.put("currencies", Map.of("GBP", Map.of("symbol", "£", "name", "British pound")));
        countryData.put("languages", Map.of("eng", "English"));
        
        List<Map<String, Object>> response = List.of(countryData);
        when(mockResponseSpec.body(List.class)).thenReturn(response);

        // When
        EnrichmentData data = client.fetchCountryData("GB");

        // Then
        assertThat(data).isNotNull();
        assertThat(data.population()).isEqualTo("67000000");
        assertThat(data.currency()).isEqualTo("GBP");
        assertThat(data.language()).isEqualTo("English");
    }

    @Test
    void convertsPopulationNumberToString() throws Exception {
        // Given
        Map<String, Object> countryData = new HashMap<>();
        countryData.put("population", 83000000);
        countryData.put("currencies", Map.of("EUR", Map.of("symbol", "€", "name", "Euro")));
        countryData.put("languages", Map.of("deu", "German"));
        
        List<Map<String, Object>> response = List.of(countryData);
        when(mockResponseSpec.body(List.class)).thenReturn(response);

        // When
        EnrichmentData data = client.fetchCountryData("DE");

        // Then
        assertThat(data.population()).isEqualTo("83000000");
        assertThat(data.population()).isInstanceOf(String.class);
    }

    @Test
    void throwsExceptionWhenResponseIsNull() {
        // Given
        when(mockResponseSpec.body(List.class)).thenReturn(null);

        // When/Then
        assertThatThrownBy(() -> client.fetchCountryData("XX"))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("No data returned");
    }

    @Test
    void throwsExceptionWhenResponseIsEmpty() {
        // Given
        when(mockResponseSpec.body(List.class)).thenReturn(List.of());

        // When/Then
        assertThatThrownBy(() -> client.fetchCountryData("XX"))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("No data returned");
    }

    @Test
    void throwsExceptionWhenPopulationIsMissing() {
        // Given - response without population
        Map<String, Object> countryData = new HashMap<>();
        countryData.put("currencies", Map.of("EUR", Map.of("symbol", "€", "name", "Euro")));
        countryData.put("languages", Map.of("eng", "English"));
        
        List<Map<String, Object>> response = List.of(countryData);
        when(mockResponseSpec.body(List.class)).thenReturn(response);

        // When/Then
        assertThatThrownBy(() -> client.fetchCountryData("XX"))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("Population data not found");
    }

    @Test
    void throwsExceptionWhenCurrenciesAreMissing() {
        // Given - response without currencies
        Map<String, Object> countryData = new HashMap<>();
        countryData.put("population", 1000000);
        countryData.put("languages", Map.of("eng", "English"));
        
        List<Map<String, Object>> response = List.of(countryData);
        when(mockResponseSpec.body(List.class)).thenReturn(response);

        // When/Then
        assertThatThrownBy(() -> client.fetchCountryData("XX"))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("Currencies data not found");
    }

    @Test
    void throwsExceptionWhenLanguagesAreMissing() {
        // Given - response without languages
        Map<String, Object> countryData = new HashMap<>();
        countryData.put("population", 1000000);
        countryData.put("currencies", Map.of("EUR", Map.of("symbol", "€", "name", "Euro")));
        
        List<Map<String, Object>> response = List.of(countryData);
        when(mockResponseSpec.body(List.class)).thenReturn(response);

        // When/Then
        assertThatThrownBy(() -> client.fetchCountryData("XX"))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("Languages data not found");
    }

    @Test
    void throwsExceptionWhenCurrenciesObjectIsEmpty() {
        // Given - empty currencies object
        Map<String, Object> countryData = new HashMap<>();
        countryData.put("population", 1000000);
        countryData.put("currencies", Map.of());
        countryData.put("languages", Map.of("eng", "English"));
        
        List<Map<String, Object>> response = List.of(countryData);
        when(mockResponseSpec.body(List.class)).thenReturn(response);

        // When/Then
        assertThatThrownBy(() -> client.fetchCountryData("XX"))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("No currency codes found");
    }

    @Test
    void throwsExceptionWhenLanguagesObjectIsEmpty() {
        // Given - empty languages object
        Map<String, Object> countryData = new HashMap<>();
        countryData.put("population", 1000000);
        countryData.put("currencies", Map.of("EUR", Map.of("symbol", "€", "name", "Euro")));
        countryData.put("languages", Map.of());
        
        List<Map<String, Object>> response = List.of(countryData);
        when(mockResponseSpec.body(List.class)).thenReturn(response);

        // When/Then
        assertThatThrownBy(() -> client.fetchCountryData("XX"))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("No languages found");
    }

    @Test
    void throwsExceptionWhenApiCallFails() {
        // Given
        when(mockResponseSpec.body(List.class))
            .thenThrow(new RuntimeException("API call failed"));

        // When/Then
        assertThatThrownBy(() -> client.fetchCountryData("XX"))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("Failed to fetch country data");
    }
}
