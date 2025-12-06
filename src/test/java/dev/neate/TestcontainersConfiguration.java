package dev.neate;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers configuration for MongoDB.
 * 
 * This configuration provides a MongoDB container for testing purposes.
 * The @ServiceConnection annotation automatically configures Spring Boot
 * to use the container's connection details.
 */
@TestConfiguration
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDBContainer() {
        return new MongoDBContainer(DockerImageName.parse("mongo:7.0"))
            .withReplicaSet()
            .withReuse(true);
    }

    @Bean
    @ServiceConnection
    ConfluentKafkaContainer confluentKafkaContainer() {
        return new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withReuse(false);
    }

}
