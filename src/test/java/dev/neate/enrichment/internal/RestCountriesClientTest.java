package dev.neate.enrichment.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
        RestCountriesResponse countryResponse = new RestCountriesResponse(
            67000000,
            Map.of("GBP", new CurrencyInfo("£", "British pound")),
            Map.of("eng", "English")
        );
        
        List<RestCountriesResponse> response = List.of(countryResponse);
        when(mockResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(response);

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
        RestCountriesResponse countryResponse = new RestCountriesResponse(
            83000000,
            Map.of("EUR", new CurrencyInfo("€", "Euro")),
            Map.of("deu", "German")
        );
        
        List<RestCountriesResponse> response = List.of(countryResponse);
        when(mockResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(response);

        // When
        EnrichmentData data = client.fetchCountryData("DE");

        // Then
        assertThat(data.population()).isEqualTo("83000000");
        assertThat(data.population()).isInstanceOf(String.class);
    }

    @Test
    void throwsExceptionWhenResponseIsNull() {
        // Given
        when(mockResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(null);

        // When/Then
        assertThatThrownBy(() -> client.fetchCountryData("XX"))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("No data returned");
    }

    @Test
    void throwsExceptionWhenResponseIsEmpty() {
        // Given
        when(mockResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(List.of());

        // When/Then
        assertThatThrownBy(() -> client.fetchCountryData("XX"))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("No data returned");
    }

    @Test
    void throwsExceptionWhenPopulationIsMissing() {
        // Given - response without population (0 is invalid)
        RestCountriesResponse countryResponse = new RestCountriesResponse(
            0,
            Map.of("EUR", new CurrencyInfo("€", "Euro")),
            Map.of("eng", "English")
        );
        
        List<RestCountriesResponse> response = List.of(countryResponse);
        when(mockResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(response);

        // When/Then
        assertThatThrownBy(() -> client.fetchCountryData("XX"))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("Failed to fetch country data for code: XX")
            .hasStackTraceContaining("Population data is missing or zero");
    }

    @Test
    void throwsExceptionWhenCurrenciesAreMissing() {
        // Given - response without currencies
        RestCountriesResponse countryResponse = new RestCountriesResponse(
            1000000,
            null,
            Map.of("eng", "English")
        );
        
        List<RestCountriesResponse> response = List.of(countryResponse);
        when(mockResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(response);

        // When/Then
        assertThatThrownBy(() -> client.fetchCountryData("XX"))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("Failed to fetch country data for code: XX")
            .hasStackTraceContaining("No currency data found");
    }

    @Test
    void throwsExceptionWhenLanguagesAreMissing() {
        // Given - response without languages
        RestCountriesResponse countryResponse = new RestCountriesResponse(
            1000000,
            Map.of("EUR", new CurrencyInfo("€", "Euro")),
            null
        );
        
        List<RestCountriesResponse> response = List.of(countryResponse);
        when(mockResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(response);

        // When/Then
        assertThatThrownBy(() -> client.fetchCountryData("XX"))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("Failed to fetch country data for code: XX")
            .hasStackTraceContaining("No language data found");
    }

    @Test
    void throwsExceptionWhenCurrenciesObjectIsEmpty() {
        // Given - empty currencies object
        RestCountriesResponse countryResponse = new RestCountriesResponse(
            1000000,
            Map.of(),
            Map.of("eng", "English")
        );
        
        List<RestCountriesResponse> response = List.of(countryResponse);
        when(mockResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(response);

        // When/Then
        assertThatThrownBy(() -> client.fetchCountryData("XX"))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("Failed to fetch country data for code: XX")
            .hasStackTraceContaining("No currency data found");
    }

    @Test
    void throwsExceptionWhenLanguagesObjectIsEmpty() {
        // Given - empty languages object
        RestCountriesResponse countryResponse = new RestCountriesResponse(
            1000000,
            Map.of("EUR", new CurrencyInfo("€", "Euro")),
            Map.of()
        );
        
        List<RestCountriesResponse> response = List.of(countryResponse);
        when(mockResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(response);

        // When/Then
        assertThatThrownBy(() -> client.fetchCountryData("XX"))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("Failed to fetch country data for code: XX")
            .hasStackTraceContaining("No language data found");
    }

    @Test
    void throwsExceptionWhenApiCallFails() {
        // Given
        when(mockResponseSpec.body(any(ParameterizedTypeReference.class)))
            .thenThrow(new RuntimeException("API call failed"));

        // When/Then
        assertThatThrownBy(() -> client.fetchCountryData("XX"))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("Failed to fetch country data");
    }
}
