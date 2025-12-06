package dev.neate.event.internal;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Kafka configuration for the Event module.
 * 
 * This configuration enables Kafka support and relies on Spring Boot's
 * auto-configuration for producer settings defined in application.yml.
 * 
 * Configuration details:
 * - Bootstrap servers: Configurable via KAFKA_BOOTSTRAP_SERVERS environment variable
 * - Default: localhost:9092
 * - Key serializer: StringSerializer
 * - Value serializer: JsonSerializer
 * 
 * This component is internal to the Event module and not exposed to other modules.
 */
@Configuration
@EnableKafka
class KafkaConfiguration {
    // Spring Boot auto-configuration handles the rest based on application.yml
}
