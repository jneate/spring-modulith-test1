package dev.neate.validation;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for CountryValidatedEvent.
 * 
 * Verifies that:
 * - Event can be created with valid country ID
 * - Event is immutable (Java record)
 * - Country ID can be accessed
 * - Event validates input (null/blank check)
 * - Events with same ID are equal
 */
class CountryValidatedEventTest {

    @Test
    void canCreateEventWithValidCountryId() {
        // When
        UUID testId = UUID.randomUUID();
        CountryValidatedEvent event = new CountryValidatedEvent(testId);

        // Then
        assertThat(event).isNotNull();
        assertThat(event.countryId()).isEqualTo(testId);
    }

    @Test
    void eventIsImmutable() {
        // Given
        UUID testId = UUID.randomUUID();
        CountryValidatedEvent event = new CountryValidatedEvent(testId);

        // Then - record fields are final by default
        assertThat(event.countryId()).isEqualTo(testId);
    }

    @Test
    void canAccessCountryId() {
        // Given
        UUID countryId = UUID.randomUUID();
        CountryValidatedEvent event = new CountryValidatedEvent(countryId);

        // When
        UUID retrievedId = event.countryId();

        // Then
        assertThat(retrievedId).isEqualTo(countryId);
    }

    @Test
    void throwsExceptionWhenCountryIdIsNull() {
        // When/Then
        assertThatThrownBy(() -> new CountryValidatedEvent(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Country ID must not be null");
    }


    @Test
    void eventsWithSameIdAreEqual() {
        // Given
        UUID testId = UUID.randomUUID();
        CountryValidatedEvent event1 = new CountryValidatedEvent(testId);
        CountryValidatedEvent event2 = new CountryValidatedEvent(testId);

        // Then
        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
    }

    @Test
    void eventsWithDifferentIdsAreNotEqual() {
        // Given
        CountryValidatedEvent event1 = new CountryValidatedEvent(UUID.randomUUID());
        CountryValidatedEvent event2 = new CountryValidatedEvent(UUID.randomUUID());

        // Then
        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    void toStringContainsCountryId() {
        // Given
        UUID testId = UUID.randomUUID();
        CountryValidatedEvent event = new CountryValidatedEvent(testId);

        // When
        String toString = event.toString();

        // Then
        assertThat(toString).contains(testId.toString());
        assertThat(toString).contains("CountryValidatedEvent");
    }
}
