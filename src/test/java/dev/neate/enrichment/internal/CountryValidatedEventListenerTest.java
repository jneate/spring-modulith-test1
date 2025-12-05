package dev.neate.enrichment.internal;

import dev.neate.domain.Country;
import dev.neate.domain.CountryService;
import dev.neate.enrichment.CountryEnrichedEvent;
import dev.neate.validation.CountryValidatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for CountryValidatedEventListener.
 * 
 * Verifies that:
 * - Listener responds to CountryValidatedEvent
 * - Only valid countries are enriched
 * - Enrichment service is called for valid countries
 * - CountryEnrichedEvent is published after successful enrichment
 * - Invalid countries are skipped
 * - Missing countries are handled gracefully
 * - Exceptions are propagated for retry
 */
class CountryValidatedEventListenerTest {

    private CountryService mockCountryService;
    private CountryEnrichmentService mockEnrichmentService;
    private ApplicationEventPublisher mockEventPublisher;
    private CountryValidatedEventListener listener;

    @BeforeEach
    void setUp() {
        mockCountryService = mock(CountryService.class);
        mockEnrichmentService = mock(CountryEnrichmentService.class);
        mockEventPublisher = mock(ApplicationEventPublisher.class);
        
        listener = new CountryValidatedEventListener(
            mockCountryService,
            mockEnrichmentService,
            mockEventPublisher
        );
    }

    @Test
    void enrichesValidCountry() throws Exception {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("United Kingdom", "GB");
        country.setId(countryId);
        country.setValidCountry(true);
        
        CountryValidatedEvent event = new CountryValidatedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));

        // When
        listener.handleCountryValidated(event);

        // Then
        verify(mockEnrichmentService).enrichCountry(country);
    }

    @Test
    void publishesEventAfterSuccessfulEnrichment() throws Exception {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("France", "FR");
        country.setId(countryId);
        country.setValidCountry(true);
        
        CountryValidatedEvent event = new CountryValidatedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));

        // When
        listener.handleCountryValidated(event);

        // Then
        verify(mockEventPublisher).publishEvent(any(CountryEnrichedEvent.class));
    }

    @Test
    void publishesEventWithCorrectCountryId() throws Exception {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("Germany", "DE");
        country.setId(countryId);
        country.setValidCountry(true);
        
        CountryValidatedEvent event = new CountryValidatedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));

        // When
        listener.handleCountryValidated(event);

        // Then
        verify(mockEventPublisher).publishEvent(argThat((Object e) -> 
            e instanceof CountryEnrichedEvent && 
            ((CountryEnrichedEvent) e).countryId().equals(countryId)
        ));
    }

    @Test
    void skipsEnrichmentForInvalidCountry() throws Exception {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("Invalid Country", "XX");
        country.setId(countryId);
        country.setValidCountry(false);
        
        CountryValidatedEvent event = new CountryValidatedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));

        // When
        listener.handleCountryValidated(event);

        // Then
        verify(mockEnrichmentService, never()).enrichCountry(any(Country.class));
        verify(mockEventPublisher, never()).publishEvent(any());
    }

    @Test
    void handlesCountryNotFound() throws Exception {
        // Given
        UUID countryId = UUID.randomUUID();
        CountryValidatedEvent event = new CountryValidatedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.empty());

        // When
        listener.handleCountryValidated(event);

        // Then
        verify(mockEnrichmentService, never()).enrichCountry(any(Country.class));
        verify(mockEventPublisher, never()).publishEvent(any());
    }

    @Test
    void propagatesEnrichmentException() throws Exception {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("Spain", "ES");
        country.setId(countryId);
        country.setValidCountry(true);
        
        CountryValidatedEvent event = new CountryValidatedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));
        
        EnrichmentException expectedException = new EnrichmentException("API failure");
        doThrow(expectedException).when(mockEnrichmentService).enrichCountry(country);

        // When/Then
        assertThatThrownBy(() -> listener.handleCountryValidated(event))
            .isSameAs(expectedException);
        
        // Verify event was not published
        verify(mockEventPublisher, never()).publishEvent(any());
    }

    @Test
    void doesNotPublishEventWhenEnrichmentFails() throws Exception {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("Italy", "IT");
        country.setId(countryId);
        country.setValidCountry(true);
        
        CountryValidatedEvent event = new CountryValidatedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));
        doThrow(new EnrichmentException("Network error"))
            .when(mockEnrichmentService).enrichCountry(country);

        // When/Then
        assertThatThrownBy(() -> listener.handleCountryValidated(event))
            .isInstanceOf(EnrichmentException.class);
        
        verify(mockEventPublisher, never()).publishEvent(any());
    }

    @Test
    void enrichesMultipleValidCountries() throws Exception {
        // Given
        UUID countryId1 = UUID.randomUUID();
        Country country1 = new Country("Portugal", "PT");
        country1.setId(countryId1);
        country1.setValidCountry(true);
        
        UUID countryId2 = UUID.randomUUID();
        Country country2 = new Country("Belgium", "BE");
        country2.setId(countryId2);
        country2.setValidCountry(true);
        
        CountryValidatedEvent event1 = new CountryValidatedEvent(countryId1);
        CountryValidatedEvent event2 = new CountryValidatedEvent(countryId2);
        
        when(mockCountryService.findById(countryId1)).thenReturn(Optional.of(country1));
        when(mockCountryService.findById(countryId2)).thenReturn(Optional.of(country2));

        // When
        listener.handleCountryValidated(event1);
        listener.handleCountryValidated(event2);

        // Then
        verify(mockEnrichmentService).enrichCountry(country1);
        verify(mockEnrichmentService).enrichCountry(country2);
        verify(mockEventPublisher, times(2)).publishEvent(any(CountryEnrichedEvent.class));
    }
}
