package dev.neate;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers configuration for Kafka only.
 * 
 * This configuration provides only a Kafka container for tests that need Kafka
 * but already have MongoDB configured elsewhere.
 * The @ServiceConnection annotation automatically configures Spring Boot
 * to use the container's connection details.
 * 
 * Use this for:
 * - Kafka-specific tests that also import MongoTestcontainersConfiguration
 * - Integration tests needing both containers (import both configs)
 */
@TestConfiguration
public class KafkaTestcontainersConfiguration {

    @Bean
    @ServiceConnection
    ConfluentKafkaContainer confluentKafkaContainer() {
        // Spring Boot @ServiceConnection automatically manages container lifecycle
        // No need to manually close - containers are reused across tests
        return new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withReuse(false);
    }

}
