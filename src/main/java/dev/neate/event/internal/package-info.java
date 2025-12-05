/**
 * Event Module - Internal implementation details.
 * 
 * This package contains internal components that should not be accessed
 * directly by other modules:
 * - CountryKafkaProducer
 * - CountryEnrichedEventListener
 * - Kafka configuration
 * 
 * Spring Modulith enforces that these components remain encapsulated within
 * the event module.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Event Internal"
)
package dev.neate.event.internal;
