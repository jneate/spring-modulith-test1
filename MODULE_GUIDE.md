# Contributing to Spring Modulith Test 1

This guide will help you extend the Spring Modulith Test 1 project with new modules and functionality. The project demonstrates event-driven architecture using Spring Boot 4.0.0 and Spring Modulith 2.0.0.

## Quick Start: Adding a New Module

1. Create package structure following Spring Modulith conventions
2. Define events in the public package
3. Implement listeners in the internal package
4. Add comprehensive tests
5. Update documentation

## Concrete Example: Adding a Notification Module

Let's add a **Notification Module** that sends email notifications when countries are enriched.

### Step 1: Create Package Structure

```
src/main/java/dev/neate/notification/
├── CountryNotificationEvent.java    # Public event class
└── internal/
    ├── CountryNotificationService.java  # Internal service
    ├── CountryEnrichedEventListener.java # Event listener
    └── EmailClient.java                  # External service client
```

### Step 2: Define the Public Event

Create `src/main/java/dev/neate/notification/CountryNotificationEvent.java`:

```java
package dev.neate.notification;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a country notification is sent.
 */
public record CountryNotificationEvent(
    UUID countryId,
    String notificationType,
    Instant notificationSentAt
) {
}
```

**Pattern**: Events are immutable records in the public package.

### Step 3: Implement the Event Listener

Create `src/main/java/dev/neate/notification/internal/CountryEnrichedEventListener.java`:

```java
package dev.neate.notification.internal;

import dev.neate.domain.CountryService;
import dev.neate.enrichment.CountryEnrichedEvent;
import dev.neate.notification.CountryNotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class CountryEnrichedEventListener {

    private static final Logger log = LoggerFactory.getLogger(CountryEnrichedEventListener.class);

    private final CountryService countryService;
    private final CountryNotificationService notificationService;

    public CountryEnrichedEventListener(CountryService countryService, 
                                       CountryNotificationService notificationService) {
        this.countryService = countryService;
        this.notificationService = notificationService;
    }

    @ApplicationModuleListener
    public void onCountryEnriched(CountryEnrichedEvent event) {
        log.debug("Processing country enriched event for notification: {}", event.countryId());
        
        try {
            var country = countryService.findById(event.countryId())
                .orElseThrow(() -> new IllegalArgumentException("Country not found: " + event.countryId()));
            
            notificationService.sendEnrichmentNotification(country);
            
        } catch (Exception e) {
            log.error("Failed to send notification for country: " + event.countryId(), e);
            throw e; // Re-throw to trigger Spring Modulith retry
        }
    }
}
```

**Patterns**:
- `@ApplicationModuleListener` for Spring Modulith event handling
- Dependency injection through constructor
- Proper error handling and logging
- Exception re-throwing for retry mechanism

### Step 4: Implement the Service

Create `src/main/java/dev/neate/notification/internal/CountryNotificationService.java`:

```java
package dev.neate.notification.internal;

import dev.neate.domain.Country;
import dev.neate.notification.CountryNotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class CountryNotificationService {

    private static final Logger log = LoggerFactory.getLogger(CountryNotificationService.class);

    private final EmailClient emailClient;
    private final ApplicationEventPublisher eventPublisher;

    public CountryNotificationService(EmailClient emailClient, 
                                     ApplicationEventPublisher eventPublisher) {
        this.emailClient = emailClient;
        this.eventPublisher = eventPublisher;
    }

    public void sendEnrichmentNotification(Country country) {
        log.info("Sending enrichment notification for country: {}", country.name());
        
        String subject = "Country Enriched: " + country.name();
        String body = buildNotificationBody(country);
        
        emailClient.sendEmail("admin@example.com", subject, body);
        
        // Publish notification event
        eventPublisher.publishEvent(new CountryNotificationEvent(
            country.id(),
            "ENRICHMENT_COMPLETE",
            java.time.Instant.now()
        ));
        
        log.info("Enrichment notification sent for country: {}", country.name());
    }

    private String buildNotificationBody(Country country) {
        return String.format("""
            Country enrichment completed:
            
            Name: %s
            Code: %s
            Population: %s
            Currency: %s
            Language: %s
            Validated: %s
            """, 
            country.name(), 
            country.code(), 
            country.population(),
            country.currency(),
            country.language(),
            country.validCountry());
    }
}
```

**Patterns**:
- Service layer with business logic
- Event publishing after successful operations
- Dependency injection for external clients
- Proper logging and error handling

### Step 5: Implement External Client

Create `src/main/java/dev/neate/notification/internal/EmailClient.java`:

```java
package dev.neate.notification.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailClient {

    private static final Logger log = LoggerFactory.getLogger(EmailClient.class);

    public void sendEmail(String to, String subject, String body) {
        log.info("Sending email to: {} with subject: {}", to, subject);
        log.debug("Email body: {}", body);
        
        // In a real implementation, this would use JavaMailSender or similar
        // For demo purposes, we just log the email content
        
        // Simulate email sending
        try {
            Thread.sleep(100); // Simulate network latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Email sent successfully to: {}", to);
    }
}
```

**Pattern**: External service clients are internal components with proper abstraction.

## Testing Patterns for New Modules

### Step 6: Create Unit Tests

Create `src/test/java/dev/neate/notification/CountryNotificationServiceTest.java`:

```java
package dev.neate.notification;

import dev.neate.domain.Country;
import dev.neate.notification.internal.CountryNotificationService;
import dev.neate.notification.internal.EmailClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
class CountryNotificationServiceTest {

    @Mock
    private EmailClient emailClient;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CountryNotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new CountryNotificationService(emailClient, eventPublisher);
    }

    @Test
    void shouldSendEnrichmentNotification() {
        // Given
        Country country = new Country(UUID.randomUUID(), "Test Country", "TC", 
            "1000000", "USD", "English", true);
        
        // When
        notificationService.sendEnrichmentNotification(country);
        
        // Then
        verify(emailClient).sendEmail(eq("admin@example.com"), any(), any());
        verify(eventPublisher).publishEvent(any(CountryNotificationEvent.class));
    }
}
```

### Step 7: Create Integration Tests

Create `src/test/java/dev/neate/notification/NotificationModuleIntegrationTest.java`:

```java
package dev.neate.notification;

import dev.neate.domain.Country;
import dev.neate.domain.CountryService;
import dev.neate.enrichment.CountryEnrichedEvent;
import dev.neate.notification.internal.CountryNotificationService;
import dev.neate.notification.internal.EmailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ApplicationModuleTest
class NotificationModuleIntegrationTest {

    @Autowired
    private CountryNotificationService notificationService;

    @MockBean
    private EmailClient emailClient;

    @MockBean
    private CountryService countryService;

    @Test
    void shouldProcessCountryEnrichedEvent(Scenario scenario) {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country(countryId, "Test Country", "TC", 
            "1000000", "USD", "English", true);
        
        when(countryService.findById(countryId)).thenReturn(java.util.Optional.of(country));

        // When
        scenario.publish(new CountryEnrichedEvent(countryId));

        // Then
        verify(emailClient).sendEmail(any(), any(), any());
    }
}
```

**Testing Patterns**:
- Unit tests with Mockito for individual components
- Integration tests with `@ApplicationModuleTest` for Spring Modulith modules
- Mock external dependencies
- Test event publishing and handling

## Module Anatomy: Learning from Existing Modules

### Validation Module Pattern

```java
// Public event (validation/CountryValidatedEvent.java)
public record CountryValidatedEvent(UUID countryId) {}

// Internal listener (validation/internal/CountryCreatedEventListener.java)
@Component
public class CountryCreatedEventListener {
    @ApplicationModuleListener
    public void onCountryCreated(CountryCreatedEvent event) {
        // Validation logic
    }
}
```

**Key Patterns**:
- Events in public package
- Listeners in internal package
- `@ApplicationModuleListener` annotation
- Constructor dependency injection

## Integration Checklist

### ✅ Spring Modulith Compliance

- [ ] Events are immutable records in public package
- [ ] Listeners are in internal package
- [ ] Use `@ApplicationModuleListener` annotation
- [ ] Dependencies injected through constructors
- [ ] Proper exception handling for retry mechanism

### ✅ Testing Requirements

- [ ] Unit tests for all service classes
- [ ] Integration tests with `@ApplicationModuleTest`
- [ ] Mock external dependencies
- [ ] Test event publishing and handling
- [ ] Test error scenarios and retry behavior

### ✅ Code Quality

- [ ] Follow YAGNI principles
- [ ] Proper logging with appropriate levels
- [ ] Clean error handling
- [ ] No unused imports or code
- [ ] Consistent naming conventions

### ✅ Documentation Updates

- [ ] Update README.md module list
- [ ] Add new event to event flow diagram
- [ ] Update project structure documentation
- [ ] Add configuration examples if needed

## Common Pitfalls and Solutions

### 1. Event Not Being Received

**Problem**: Event listener not triggered
**Solution**: 
- Verify `@ApplicationModuleListener` annotation
- Check package structure (public vs internal)
- Ensure event class is in correct package

### 2. Retry Not Working

**Problem**: Failed events not retried
**Solution**:
- Re-throw exceptions from listeners
- Check Spring Modulith retry configuration
- Verify MongoDB replica set is running

### 3. Test Failures

**Problem**: Integration tests not finding events
**Solution**:
- Use `@ApplicationModuleTest` for module tests
- Mock external dependencies properly
- Verify event publication in tests

## Development Workflow

1. **Create package structure** following Spring Modulith conventions
2. **Implement event classes** as immutable records
3. **Create listeners** with `@ApplicationModuleListener`
4. **Write tests** before implementing business logic
5. **Test integration** with existing modules
6. **Update documentation** and diagrams
7. **Run full test suite** to ensure no regressions

## Code Style Guidelines

- Use Java records for immutable data structures
- Follow Spring Boot dependency injection patterns
- Implement proper logging with SLF4J
- Handle exceptions appropriately for retry mechanisms
- Keep classes focused on single responsibilities
- Follow YAGNI principles - don't over-engineer

## Getting Help

- Check existing modules for patterns
- Review Spring Modulith documentation
- Look at test examples in the project
- Ensure MongoDB replica set is running for event publication
- Check application logs for event processing issues

Happy coding! 🚀
