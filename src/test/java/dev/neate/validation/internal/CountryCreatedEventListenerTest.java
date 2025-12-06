package dev.neate.validation.internal;

import dev.neate.api.CountryCreatedEvent;
import dev.neate.domain.Country;
import dev.neate.domain.CountryService;
import dev.neate.validation.CountryValidatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for CountryCreatedEventListener.
 * 
 * Verifies that:
 * - Listener responds to CountryCreatedEvent
 * - Country is fetched from domain service
 * - Country is validated using validation service
 * - Valid countries are saved and published as validated events
 * - Invalid countries are not saved and no events are published
 * - Missing countries are handled gracefully
 * - Exceptions are propagated for retry
 */
class CountryCreatedEventListenerTest {

    private CountryService mockCountryService;
    private CountryValidationService mockValidationService;
    private ApplicationEventPublisher mockEventPublisher;
    private CountryCreatedEventListener listener;

    @BeforeEach
    void setUp() {
        mockCountryService = mock(CountryService.class);
        mockValidationService = mock(CountryValidationService.class);
        mockEventPublisher = mock(ApplicationEventPublisher.class);
        
        listener = new CountryCreatedEventListener(
            mockCountryService,
            mockValidationService,
            mockEventPublisher
        );
    }

    @Test
    void validatesAndSavesValidCountry() {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("United Kingdom", "GB");
        country.setId(countryId);
        country.setValidCountry(false); // Initially false
        
        CountryCreatedEvent event = new CountryCreatedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));
        when(mockValidationService.validate(country)).thenReturn(true);
        when(mockCountryService.save(any(Country.class))).thenReturn(country);

        // When
        listener.handleCountryCreated(event);

        // Then
        verify(mockCountryService).findById(countryId);
        verify(mockValidationService).validate(country);
        
        // Verify country was marked as valid before saving
        assertThat(country.getValidCountry()).isTrue();
        verify(mockCountryService).save(country);
        
        // Verify event was published
        verify(mockEventPublisher).publishEvent(any(CountryValidatedEvent.class));
    }

    @Test
    void publishesEventWithCorrectCountryId() {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("France", "FR");
        country.setId(countryId);
        
        CountryCreatedEvent event = new CountryCreatedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));
        when(mockValidationService.validate(country)).thenReturn(true);
        when(mockCountryService.save(any(Country.class))).thenReturn(country);

        // When
        listener.handleCountryCreated(event);

        // Then
        verify(mockEventPublisher).publishEvent(argThat((Object e) -> 
            e instanceof CountryValidatedEvent && 
            ((CountryValidatedEvent) e).countryId().equals(countryId)
        ));
    }

    @Test
    void doesNotSaveInvalidCountry() {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("Invalid Country", "XX");
        country.setId(countryId);
        country.setValidCountry(false);
        
        CountryCreatedEvent event = new CountryCreatedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));
        when(mockValidationService.validate(country)).thenReturn(false);

        // When
        listener.handleCountryCreated(event);

        // Then
        verify(mockCountryService).findById(countryId);
        verify(mockValidationService).validate(country);
        
        // Verify country was NOT saved
        verify(mockCountryService, never()).save(any(Country.class));
        
        // Verify event was NOT published
        verify(mockEventPublisher, never()).publishEvent(any());
        
        // Verify country remains invalid
        assertThat(country.getValidCountry()).isFalse();
    }

    @Test
    void handlesCountryNotFound() {
        // Given
        UUID countryId = UUID.randomUUID();
        CountryCreatedEvent event = new CountryCreatedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.empty());

        // When
        listener.handleCountryCreated(event);

        // Then
        verify(mockCountryService).findById(countryId);
        
        // Verify validation was not called
        verify(mockValidationService, never()).validate(any());
        
        // Verify country was not saved
        verify(mockCountryService, never()).save(any(Country.class));
        
        // Verify event was not published
        verify(mockEventPublisher, never()).publishEvent(any());
    }

    @Test
    void propagatesValidationServiceException() {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("Spain", "ES");
        country.setId(countryId);
        
        CountryCreatedEvent event = new CountryCreatedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));
        
        RuntimeException expectedException = new RuntimeException("Validation service error");
        doThrow(expectedException).when(mockValidationService).validate(country);

        // When/Then
        assertThatThrownBy(() -> listener.handleCountryCreated(event))
            .isSameAs(expectedException);
        
        // Verify save was not called due to exception
        verify(mockCountryService, never()).save(any(Country.class));
        
        // Verify event was not published due to exception
        verify(mockEventPublisher, never()).publishEvent(any());
    }

    @Test
    void propagatesCountryServiceException() {
        // Given
        UUID countryId = UUID.randomUUID();
        CountryCreatedEvent event = new CountryCreatedEvent(countryId);
        
        RuntimeException expectedException = new RuntimeException("Country service error");
        doThrow(expectedException).when(mockCountryService).findById(countryId);

        // When/Then
        assertThatThrownBy(() -> listener.handleCountryCreated(event))
            .isSameAs(expectedException);
        
        // Verify validation was not called due to exception
        verify(mockValidationService, never()).validate(any());
        
        // Verify save was not called due to exception
        verify(mockCountryService, never()).save(any(Country.class));
        
        // Verify event was not published due to exception
        verify(mockEventPublisher, never()).publishEvent(any());
    }

    @Test
    void propagatesSaveException() {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("Italy", "IT");
        country.setId(countryId);
        
        CountryCreatedEvent event = new CountryCreatedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));
        when(mockValidationService.validate(country)).thenReturn(true);
        
        RuntimeException expectedException = new RuntimeException("Save operation failed");
        doThrow(expectedException).when(mockCountryService).save(country);

        // When/Then
        assertThatThrownBy(() -> listener.handleCountryCreated(event))
            .isSameAs(expectedException);
        
        // Verify event was not published due to exception
        verify(mockEventPublisher, never()).publishEvent(any());
    }

    @Test
    void handlesMultipleValidCountries() {
        // Given
        UUID countryId1 = UUID.randomUUID();
        Country country1 = new Country("Portugal", "PT");
        country1.setId(countryId1);
        
        UUID countryId2 = UUID.randomUUID();
        Country country2 = new Country("Belgium", "BE");
        country2.setId(countryId2);
        
        CountryCreatedEvent event1 = new CountryCreatedEvent(countryId1);
        CountryCreatedEvent event2 = new CountryCreatedEvent(countryId2);
        
        when(mockCountryService.findById(countryId1)).thenReturn(Optional.of(country1));
        when(mockCountryService.findById(countryId2)).thenReturn(Optional.of(country2));
        when(mockValidationService.validate(country1)).thenReturn(true);
        when(mockValidationService.validate(country2)).thenReturn(true);
        when(mockCountryService.save(any(Country.class))).thenReturn(country1, country2);

        // When
        listener.handleCountryCreated(event1);
        listener.handleCountryCreated(event2);

        // Then
        verify(mockCountryService).findById(countryId1);
        verify(mockCountryService).findById(countryId2);
        verify(mockValidationService).validate(country1);
        verify(mockValidationService).validate(country2);
        verify(mockCountryService, times(2)).save(any(Country.class));
        verify(mockEventPublisher, times(2)).publishEvent(any(CountryValidatedEvent.class));
        
        // Verify both countries were marked as valid
        assertThat(country1.getValidCountry()).isTrue();
        assertThat(country2.getValidCountry()).isTrue();
    }

    @Test
    void validatesWithExactCountryObject() {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("Germany", "DE");
        country.setId(countryId);
        
        CountryCreatedEvent event = new CountryCreatedEvent(countryId);
        when(mockCountryService.findById(countryId)).thenReturn(Optional.of(country));
        when(mockValidationService.validate(country)).thenReturn(true);
        when(mockCountryService.save(any(Country.class))).thenReturn(country);

        // When
        listener.handleCountryCreated(event);

        // Then
        verify(mockValidationService).validate(country);
    }
}
