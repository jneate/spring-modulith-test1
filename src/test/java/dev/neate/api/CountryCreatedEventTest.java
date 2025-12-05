package dev.neate.api;

import org.junit.jupiter.api.Test;

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
        CountryCreatedEvent event = new CountryCreatedEvent("country-123");
        
        assertThat(event)
            .as("Event should be created")
            .isNotNull();
        
        assertThat(event.countryId())
            .as("Country ID should be set")
            .isEqualTo("country-123");
    }

    @Test
    void eventIsImmutable() {
        // Create event
        CountryCreatedEvent event = new CountryCreatedEvent("country-456");
        
        // Verify it's a record (immutable)
        assertThat(event.getClass().isRecord())
            .as("Event should be a record")
            .isTrue();
    }

    @Test
    void eventsWithSameIdAreEqual() {
        // Create two events with same ID
        CountryCreatedEvent event1 = new CountryCreatedEvent("country-789");
        CountryCreatedEvent event2 = new CountryCreatedEvent("country-789");
        
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
        CountryCreatedEvent event1 = new CountryCreatedEvent("country-111");
        CountryCreatedEvent event2 = new CountryCreatedEvent("country-222");
        
        assertThat(event1)
            .as("Events with different IDs should not be equal")
            .isNotEqualTo(event2);
    }

    @Test
    void toStringContainsCountryId() {
        // Create event
        CountryCreatedEvent event = new CountryCreatedEvent("country-abc");
        
        String toString = event.toString();
        
        assertThat(toString)
            .as("toString should contain country ID")
            .contains("country-abc");
    }
}
