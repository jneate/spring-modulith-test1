package dev.neate.event.internal;

import dev.neate.domain.Country;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer service for publishing country events.
 * 
 * This service sends country data to Kafka topic 'country-events' using JSON serialization.
 * The key is the country ID (as string) and the value is the complete country object.
 * 
 * Error handling:
 * - Throws KafkaException on send failures
 * - Exceptions trigger Spring Modulith retry mechanism
 * 
 * This component is internal to the Event module and not exposed to other modules.
 */
@Service
class CountryKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(CountryKafkaProducer.class);
    private static final String TOPIC = "country-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Constructor injection of KafkaTemplate.
     *
     * @param kafkaTemplate Spring Kafka template for sending messages
     */
    public CountryKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Sends country event to Kafka.
     * 
     * The country object is serialized to JSON and sent to the 'country-events' topic.
     * The country ID is used as the message key for partitioning.
     * 
     * Event payload structure:
     * {
     *   "id": "country-id",
     *   "name": "country-name",
     *   "code": "country-code",
     *   "currency": "country-currency",
     *   "language": "country-language",
     *   "population": "country-population",
     *   "validCountry": true/false
     * }
     *
     * @param country the country to send
     * @throws org.springframework.kafka.KafkaException if send fails
     */
    public void sendCountryEvent(Country country) {
        String key = country.getId().toString();
        
        log.debug("Sending country event to Kafka: {} ({})", country.getName(), country.getCode());
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(TOPIC, key, country);
        
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send country event to Kafka for country: {} ({})", 
                    country.getName(), country.getCode(), ex);
                // Exception will be propagated for retry
                throw new org.springframework.kafka.KafkaException(
                    "Failed to send country event to Kafka", ex);
            } else {
                log.info("Successfully sent country event to Kafka: {} ({}) - partition: {}, offset: {}", 
                    country.getName(), 
                    country.getCode(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            }
        });
        
        // Block to ensure synchronous behavior and exception propagation
        try {
            future.get();
        } catch (Exception e) {
            log.error("Error waiting for Kafka send result", e);
            throw new org.springframework.kafka.KafkaException("Failed to send country event to Kafka", e);
        }
    }
}
