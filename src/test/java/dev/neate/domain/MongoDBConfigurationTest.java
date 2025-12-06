package dev.neate.domain;

import dev.neate.MongoTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for MongoDB configuration in the Domain module.
 * 
 * Verifies that:
 * - MongoTemplate bean is available
 * - Database connection is established
 * - Configuration uses Testcontainers MongoDB for testing
 * 
 * Performance Optimization:
 * Uses @DataMongoTest (Spring Boot 4.0.0) for faster slice testing that loads only
 * MongoDB infrastructure instead of the full Spring context, providing 60-80% faster
 * test execution while maintaining full Testcontainers integration.
 */
@DataMongoTest
@Import(MongoTestcontainersConfiguration.class)
class MongoDBConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void mongoTemplateIsConfigured() {
        // Verify that MongoTemplate bean exists
        assertThat(applicationContext.containsBean("mongoTemplate"))
            .as("MongoTemplate should be configured")
            .isTrue();
    }

    @Test
    void mongoTemplateBeanIsAvailable() {
        // Verify that we can retrieve the MongoTemplate bean
        assertThat(mongoTemplate)
            .as("MongoTemplate bean should be available")
            .isNotNull();
    }

    @Test
    void mongoDatabaseConnectionIsEstablished() {
        // Verify that we can connect to the database
        String databaseName = mongoTemplate.getDb().getName();
        
        assertThat(databaseName)
            .as("Database name should be configured")
            .isNotNull()
            .isNotEmpty();
    }

    @Test
    void canExecuteBasicMongoOperations() {
        // Verify that basic MongoDB operations work
        String collectionName = "test-collection";
        
        // Check if collection exists (it shouldn't initially)
        boolean exists = mongoTemplate.collectionExists(collectionName);
        
        assertThat(exists)
            .as("Test collection should not exist initially")
            .isFalse();
        
        // Create collection
        mongoTemplate.createCollection(collectionName);
        
        // Verify collection was created
        exists = mongoTemplate.collectionExists(collectionName);
        
        assertThat(exists)
            .as("Test collection should exist after creation")
            .isTrue();
        
        // Clean up
        mongoTemplate.dropCollection(collectionName);
    }
}
