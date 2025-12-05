package dev.neate.domain.internal.config;

import com.mongodb.MongoClientSettings;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB configuration for the Domain module.
 * 
 * This configuration:
 * - Enables MongoDB repositories for the domain.internal package
 * - Configures UUID representation for proper UUID storage
 * - Integrates with Spring Modulith's event publication registry
 * 
 * The repository scanning is limited to the internal package to maintain
 * proper encapsulation - only the internal implementation has direct
 * database access.
 * 
 * Connection details (URI, database name) are configured in application.yml
 * and can be overridden via environment variables.
 */
@Configuration
@EnableMongoRepositories(basePackages = "dev.neate.domain.internal")
public class MongoDBConfiguration extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "country-db";
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        builder.uuidRepresentation(org.bson.UuidRepresentation.STANDARD);
    }
}
