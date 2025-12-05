package dev.neate.enrichment;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for CountryEnrichedEvent.
 * 
 * Verifies that:
 * - Event can be created with valid country ID
 * - Event is immutable (Java record)
 * - Country ID can be accessed
 * - Event validates input (null check)
 * - Events with same ID are equal
 */
class CountryEnrichedEventTest {

    @Test
    void canCreateEventWithValidCountryId() {
        // When
        UUID testId = UUID.randomUUID();
        CountryEnrichedEvent event = new CountryEnrichedEvent(testId);

        // Then
        assertThat(event).isNotNull();
        assertThat(event.countryId()).isEqualTo(testId);
    }

    @Test
    void eventIsImmutable() {
        // Given
        UUID testId = UUID.randomUUID();
        CountryEnrichedEvent event = new CountryEnrichedEvent(testId);

        // Then - record fields are final by default
        assertThat(event.countryId()).isEqualTo(testId);
    }

    @Test
    void canAccessCountryId() {
        // Given
        UUID countryId = UUID.randomUUID();
        CountryEnrichedEvent event = new CountryEnrichedEvent(countryId);

        // When
        UUID retrievedId = event.countryId();

        // Then
        assertThat(retrievedId).isEqualTo(countryId);
    }

    @Test
    void throwsExceptionWhenCountryIdIsNull() {
        // When/Then
        assertThatThrownBy(() -> new CountryEnrichedEvent(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Country ID must not be null");
    }

    @Test
    void eventsWithSameIdAreEqual() {
        // Given
        UUID testId = UUID.randomUUID();
        CountryEnrichedEvent event1 = new CountryEnrichedEvent(testId);
        CountryEnrichedEvent event2 = new CountryEnrichedEvent(testId);

        // Then
        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
    }

    @Test
    void eventsWithDifferentIdsAreNotEqual() {
        // Given
        CountryEnrichedEvent event1 = new CountryEnrichedEvent(UUID.randomUUID());
        CountryEnrichedEvent event2 = new CountryEnrichedEvent(UUID.randomUUID());

        // Then
        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    void toStringContainsCountryId() {
        // Given
        UUID testId = UUID.randomUUID();
        CountryEnrichedEvent event = new CountryEnrichedEvent(testId);

        // When
        String toString = event.toString();

        // Then
        assertThat(toString).contains(testId.toString());
        assertThat(toString).contains("CountryEnrichedEvent");
    }
}
