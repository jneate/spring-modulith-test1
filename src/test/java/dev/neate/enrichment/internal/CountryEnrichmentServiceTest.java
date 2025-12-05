package dev.neate.enrichment.internal;

import dev.neate.domain.Country;
import dev.neate.domain.CountryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for CountryEnrichmentService.
 * 
 * Verifies that:
 * - Service enriches countries with API data
 * - Country fields are updated correctly
 * - Enriched country is saved
 * - Exceptions are propagated
 */
class CountryEnrichmentServiceTest {

    private RestCountriesClient mockClient;
    private CountryService mockCountryService;
    private CountryEnrichmentService service;

    @BeforeEach
    void setUp() {
        mockClient = mock(RestCountriesClient.class);
        mockCountryService = mock(CountryService.class);
        service = new CountryEnrichmentService(mockClient, mockCountryService);
    }

    @Test
    void enrichesCountryWithApiData() throws Exception {
        // Given
        Country country = new Country("United Kingdom", "GB");
        country.setId(UUID.randomUUID());
        
        EnrichmentData data = new EnrichmentData("67000000", "GBP", "English");
        when(mockClient.fetchCountryData("GB")).thenReturn(data);
        when(mockCountryService.save(any(Country.class))).thenReturn(country);

        // When
        service.enrichCountry(country);

        // Then
        assertThat(country.getPopulation()).isEqualTo("67000000");
        assertThat(country.getCurrency()).isEqualTo("GBP");
        assertThat(country.getLanguage()).isEqualTo("English");
    }

    @Test
    void savesEnrichedCountry() throws Exception {
        // Given
        Country country = new Country("France", "FR");
        country.setId(UUID.randomUUID());
        
        EnrichmentData data = new EnrichmentData("67000000", "EUR", "French");
        when(mockClient.fetchCountryData("FR")).thenReturn(data);
        when(mockCountryService.save(any(Country.class))).thenReturn(country);

        // When
        service.enrichCountry(country);

        // Then
        verify(mockCountryService).save(country);
    }

    @Test
    void usesCountryCodeToFetchData() throws Exception {
        // Given
        Country country = new Country("Germany", "DE");
        country.setId(UUID.randomUUID());
        
        EnrichmentData data = new EnrichmentData("83000000", "EUR", "German");
        when(mockClient.fetchCountryData("DE")).thenReturn(data);
        when(mockCountryService.save(any(Country.class))).thenReturn(country);

        // When
        service.enrichCountry(country);

        // Then
        verify(mockClient).fetchCountryData("DE");
    }

    @Test
    void updatesAllEnrichmentFields() throws Exception {
        // Given
        Country country = new Country("Spain", "ES");
        country.setId(UUID.randomUUID());
        
        EnrichmentData data = new EnrichmentData("47000000", "EUR", "Spanish");
        when(mockClient.fetchCountryData("ES")).thenReturn(data);
        when(mockCountryService.save(any(Country.class))).thenReturn(country);

        // When
        service.enrichCountry(country);

        // Then
        assertThat(country.getPopulation()).isEqualTo("47000000");
        assertThat(country.getCurrency()).isEqualTo("EUR");
        assertThat(country.getLanguage()).isEqualTo("Spanish");
        verify(mockCountryService).save(country);
    }

    @Test
    void throwsExceptionWhenApiFails() throws Exception {
        // Given
        Country country = new Country("Italy", "IT");
        country.setId(UUID.randomUUID());
        
        when(mockClient.fetchCountryData("IT"))
            .thenThrow(new EnrichmentException("API call failed"));

        // When/Then
        assertThatThrownBy(() -> service.enrichCountry(country))
            .isInstanceOf(EnrichmentException.class)
            .hasMessageContaining("API call failed");
        
        // Verify country was not saved
        verify(mockCountryService, never()).save(any(Country.class));
    }

    @Test
    void propagatesEnrichmentException() throws Exception {
        // Given
        Country country = new Country("Portugal", "PT");
        country.setId(UUID.randomUUID());
        
        EnrichmentException expectedException = new EnrichmentException("Network error");
        when(mockClient.fetchCountryData("PT")).thenThrow(expectedException);

        // When/Then
        assertThatThrownBy(() -> service.enrichCountry(country))
            .isSameAs(expectedException);
    }

    @Test
    void enrichesCountryWithDifferentData() throws Exception {
        // Given
        Country country = new Country("Belgium", "BE");
        country.setId(UUID.randomUUID());
        
        EnrichmentData data = new EnrichmentData("11500000", "EUR", "Dutch");
        when(mockClient.fetchCountryData("BE")).thenReturn(data);
        when(mockCountryService.save(any(Country.class))).thenReturn(country);

        // When
        service.enrichCountry(country);

        // Then
        assertThat(country.getPopulation()).isEqualTo("11500000");
        assertThat(country.getCurrency()).isEqualTo("EUR");
        assertThat(country.getLanguage()).isEqualTo("Dutch");
    }

    @Test
    void handlesLargePopulationNumbers() throws Exception {
        // Given
        Country country = new Country("China", "CN");
        country.setId(UUID.randomUUID());
        
        EnrichmentData data = new EnrichmentData("1400000000", "CNY", "Chinese");
        when(mockClient.fetchCountryData("CN")).thenReturn(data);
        when(mockCountryService.save(any(Country.class))).thenReturn(country);

        // When
        service.enrichCountry(country);

        // Then
        assertThat(country.getPopulation()).isEqualTo("1400000000");
    }
}
