package dev.neate.event.internal;

import dev.neate.domain.Country;
import dev.neate.domain.CountryService;
import dev.neate.enrichment.CountryEnrichedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.KafkaException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for CountryEnrichedEventListener.
 * 
 * Verifies that:
 * - Listener responds to CountryEnrichedEvent
 * - Country data is fetched from domain
 * - Country is sent to Kafka via producer
 * - Missing countries are handled gracefully
 * - Exceptions are propagated for retry
 */
class CountryEnrichedEventListenerTest {

    private CountryService mockCountryService;
    private CountryKafkaProducer mockKafkaProducer;
    private CountryEnrichedEventListener listener;

    @BeforeEach
    void setUp() {
        mockCountryService = mock(CountryService.class);
        mockKafkaProducer = mock(CountryKafkaProducer.class);
        
        listener = new CountryEnrichedEventListener(
            mockCountryService,
            mockKafkaProducer
        );
    }

    @Test
    void sendsEnrichedCountryToKafka() {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("United Kingdom", "GB");
        country.setId(countryId);
        country.setPopulation("67000000");
        country.setCurrency("GBP");
        country.setLanguage("English");
        country.setValidCountry(true);
        
        CountryEnrichedEvent event = new CountryEnrichedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));

        // When
        listener.handleCountryEnriched(event);

        // Then
        verify(mockKafkaProducer).sendCountryEvent(country);
    }

    @Test
    void fetchesCountryFromDomain() {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("France", "FR");
        country.setId(countryId);
        
        CountryEnrichedEvent event = new CountryEnrichedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));

        // When
        listener.handleCountryEnriched(event);

        // Then
        verify(mockCountryService).findById(countryId);
    }

    @Test
    void handlesCountryNotFound() {
        // Given
        UUID countryId = UUID.randomUUID();
        CountryEnrichedEvent event = new CountryEnrichedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.empty());

        // When
        listener.handleCountryEnriched(event);

        // Then
        verify(mockKafkaProducer, never()).sendCountryEvent(any(Country.class));
    }

    @Test
    void propagatesKafkaException() {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("Germany", "DE");
        country.setId(countryId);
        
        CountryEnrichedEvent event = new CountryEnrichedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));
        
        KafkaException expectedException = new KafkaException("Kafka connection failed");
        doThrow(expectedException).when(mockKafkaProducer).sendCountryEvent(country);

        // When/Then
        assertThatThrownBy(() -> listener.handleCountryEnriched(event))
            .isSameAs(expectedException);
    }

    @Test
    void sendsCompleteEnrichedData() {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("Spain", "ES");
        country.setId(countryId);
        country.setPopulation("47000000");
        country.setCurrency("EUR");
        country.setLanguage("Spanish");
        country.setValidCountry(true);
        
        CountryEnrichedEvent event = new CountryEnrichedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));

        // When
        listener.handleCountryEnriched(event);

        // Then
        verify(mockKafkaProducer).sendCountryEvent(country);
    }

    @Test
    void handlesMultipleEvents() {
        // Given
        UUID countryId1 = UUID.randomUUID();
        Country country1 = new Country("Italy", "IT");
        country1.setId(countryId1);
        
        UUID countryId2 = UUID.randomUUID();
        Country country2 = new Country("Portugal", "PT");
        country2.setId(countryId2);
        
        CountryEnrichedEvent event1 = new CountryEnrichedEvent(countryId1);
        CountryEnrichedEvent event2 = new CountryEnrichedEvent(countryId2);
        
        when(mockCountryService.findById(countryId1)).thenReturn(Optional.of(country1));
        when(mockCountryService.findById(countryId2)).thenReturn(Optional.of(country2));

        // When
        listener.handleCountryEnriched(event1);
        listener.handleCountryEnriched(event2);

        // Then
        verify(mockKafkaProducer).sendCountryEvent(country1);
        verify(mockKafkaProducer).sendCountryEvent(country2);
    }

    @Test
    void doesNotSendToKafkaWhenCountryNotFound() {
        // Given
        UUID countryId = UUID.randomUUID();
        CountryEnrichedEvent event = new CountryEnrichedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.empty());

        // When
        listener.handleCountryEnriched(event);

        // Then
        verify(mockKafkaProducer, never()).sendCountryEvent(any());
    }

    @Test
    void propagatesExceptionForRetry() {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("Belgium", "BE");
        country.setId(countryId);
        
        CountryEnrichedEvent event = new CountryEnrichedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));
        doThrow(new KafkaException("Network error"))
            .when(mockKafkaProducer).sendCountryEvent(country);

        // When/Then
        assertThatThrownBy(() -> listener.handleCountryEnriched(event))
            .isInstanceOf(KafkaException.class);
    }
}
