package dev.neate.domain.internal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB configuration for the Domain module.
 * 
 * This configuration enables MongoDB repositories and is managed within
 * the Domain module as part of its internal infrastructure.
 * 
 * Configuration properties are externalized via application.yml:
 * - spring.data.mongodb.uri: MongoDB connection URI (default: mongodb://localhost:27017)
 * - spring.data.mongodb.database: Database name (default: country-db)
 * 
 * Spring Modulith event publication is automatically configured via
 * spring-modulith-starter-mongodb dependency, which uses the same MongoDB
 * connection for persisting event publications.
 */
@Configuration
@EnableMongoRepositories(basePackages = "dev.neate.domain.internal")
public class MongoDBConfiguration {
    // MongoDB auto-configuration from Spring Boot handles connection setup
    // Event publication registry is auto-configured by Spring Modulith
}
