package dev.neate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.events.core.EventPublicationRepository;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for Spring Modulith Event Publication Configuration.
 * 
 * Verifies that:
 * - Event publication repository is properly configured
 * - MongoDB-backed event publication is enabled
 * - Event publication beans are available in the application context
 * - Configuration uses Testcontainers MongoDB for testing
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
    "spring.data.mongodb.database=test-event-db"
})
class EventPublicationConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void eventPublicationRepositoryIsConfigured() {
        // Verify that EventPublicationRepository bean is available
        // This is the core component for event publication in Spring Modulith 2.0.0
        String[] beanNames = applicationContext.getBeanNamesForType(EventPublicationRepository.class);
        
        assertThat(beanNames)
            .as("EventPublicationRepository should be configured")
            .isNotEmpty();
    }

    @Test
    void eventPublicationRepositoryBeanExists() {
        // Verify that we can retrieve the EventPublicationRepository bean
        EventPublicationRepository repository = applicationContext.getBean(EventPublicationRepository.class);
        
        assertThat(repository)
            .as("EventPublicationRepository bean should exist")
            .isNotNull();
    }

    @Test
    void mongoDbEventPublicationConfigurationIsPresent() {
        // Verify that MongoDB event publication configuration is loaded
        // The spring-modulith-starter-mongodb should auto-configure the event publication
        String[] beanNames = applicationContext.getBeanNamesForType(EventPublicationRepository.class);
        
        assertThat(beanNames)
            .as("EventPublicationRepository should be configured with MongoDB backend")
            .hasSize(1);
    }

    @Test
    void mongoDbDataSourceIsConfigured() {
        // Verify that MongoDB-related beans are present
        // This confirms the MongoDB backend is properly configured
        boolean hasMongoTemplate = applicationContext.containsBean("mongoTemplate");
        
        assertThat(hasMongoTemplate)
            .as("MongoDB template should be configured for event publication")
            .isTrue();
    }

    @Test
    void eventPublicationRepositoryCanBeInjected() {
        // Verify that the repository can be injected and used
        EventPublicationRepository repository = applicationContext.getBean(EventPublicationRepository.class);
        
        // Verify basic functionality - findIncompletePublications should return empty initially
        var incompletePublications = repository.findIncompletePublications();
        
        assertThat(incompletePublications)
            .as("Incomplete publications should be empty initially")
            .isEmpty();
    }
}
