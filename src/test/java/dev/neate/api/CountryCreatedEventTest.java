package dev.neate.api;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for CountryCreatedEvent.
 * 
 * Verifies that:
 * - Event can be instantiated
 * - Event is immutable (record)
 * - Event fields are accessible
 * - Event equality works correctly
 */
class CountryCreatedEventTest {

    @Test
    void canCreateEvent() {
        // Create event
        UUID testId = UUID.randomUUID();
        CountryCreatedEvent event = new CountryCreatedEvent(testId);
        
        assertThat(event)
            .as("Event should be created")
            .isNotNull();
        
        assertThat(event.countryId())
            .as("Country ID should be set")
            .isEqualTo(testId);
    }

    @Test
    void eventIsImmutable() {
        // Create event
        CountryCreatedEvent event = new CountryCreatedEvent(UUID.randomUUID());
        
        // Verify it's a record (immutable)
        assertThat(event.getClass().isRecord())
            .as("Event should be a record")
            .isTrue();
    }

    @Test
    void eventsWithSameIdAreEqual() {
        // Create two events with same ID
        UUID testId = UUID.randomUUID();
        CountryCreatedEvent event1 = new CountryCreatedEvent(testId);
        CountryCreatedEvent event2 = new CountryCreatedEvent(testId);
        
        assertThat(event1)
            .as("Events with same ID should be equal")
            .isEqualTo(event2);
        
        assertThat(event1.hashCode())
            .as("Hash codes should be equal")
            .isEqualTo(event2.hashCode());
    }

    @Test
    void eventsWithDifferentIdsAreNotEqual() {
        // Create two events with different IDs
        CountryCreatedEvent event1 = new CountryCreatedEvent(UUID.randomUUID());
        CountryCreatedEvent event2 = new CountryCreatedEvent(UUID.randomUUID());
        
        assertThat(event1)
            .as("Events with different IDs should not be equal")
            .isNotEqualTo(event2);
    }

    @Test
    void toStringContainsCountryId() {
        // Create event
        UUID testId = UUID.randomUUID();
        CountryCreatedEvent event = new CountryCreatedEvent(testId);
        
        String toString = event.toString();
        
        assertThat(toString)
            .as("toString should contain country ID")
            .contains(testId.toString());
    }
}
