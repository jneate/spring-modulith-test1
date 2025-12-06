package dev.neate;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers configuration for MongoDB only.
 * 
 * This configuration provides only a MongoDB container for tests that don't need Kafka.
 * The @ServiceConnection annotation automatically configures Spring Boot
 * to use the container's connection details.
 * 
 * Use this for:
 * - Repository tests
 * - Service tests  
 * - Any test needing only MongoDB
 */
@TestConfiguration
public class MongoTestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDBContainer() {
        // Spring Boot @ServiceConnection automatically manages container lifecycle
        // No need to manually close - containers are reused across tests
        return new MongoDBContainer(DockerImageName.parse("mongo:7.0"))
            .withReplicaSet()
            .withReuse(true);
    }

}
