# Development Tasks

This document breaks down the implementation plan into individual development tasks for each module. Each task contains sufficient information for implementation without requiring context switching between modules.

---

## Application Module

### Task 1.1: Create Maven Project and Main Application Class
**Description**: Set up the Spring Boot application entry point with Spring Modulith configuration using Maven.

**Requirements**:
- Create single Maven project with Java 21
- Add Spring Boot parent POM (latest stable version)
- Add Spring Modulith dependencies to `pom.xml`
- Create main application class in package `dev.neate`
- Annotate with `@SpringBootApplication`
- Configure Spring Modulith to scan all module packages

**Project Structure**:
```
spring-modulith-test1/
├── pom.xml
└── src/main/java/dev/neate/
    ├── Application.java
    ├── api/                    (REST API - public: events, DTOs, controllers)
    ├── domain/                 (Domain - public: entity, service interface)
    │   └── internal/           (private: repository, service impl)
    ├── validation/             (Validation - public: events only)
    │   └── internal/           (private: services, listeners)
    ├── enrichment/             (Enrichment - public: events only)
    │   └── internal/           (private: clients, services, listeners)
    └── event/                  (Event - public: none)
        └── internal/           (private: producers, listeners, config)
```

**Maven Dependencies**:
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version><!-- latest stable --></version>
</parent>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.modulith</groupId>
            <artifactId>spring-modulith-bom</artifactId>
            <version><!-- latest stable --></version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-core</artifactId>
    </dependency>
</dependencies>
```

**Dependencies**: None

**Acceptance Criteria**:
- Maven project builds successfully
- Application starts successfully
- Spring Modulith is properly configured
- All modules are detected by Spring Modulith

---

## Domain Module

### Task 2.1: Configure MongoDB in Domain Module and Event Publication
**Description**: Set up MongoDB configuration within the Domain module and configure Spring Modulith's event publication registry to use MongoDB.

**Requirements**:
- Add MongoDB Spring Boot Starter dependency to `pom.xml`
- Add Spring Modulith event externalization dependency for MongoDB to `pom.xml`
- Create configuration class in `dev.neate.domain.config` package
- Configure MongoDB connection via environment variables
- Default to localhost for local development
- Enable Spring Modulith event publication registry with MongoDB backend
- Set up retry configuration for event listeners

**Maven Dependencies**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.modulith</groupId>
    <artifactId>spring-modulith-starter-mongodb</artifactId>
</dependency>
```

**Configuration Details**:
- MongoDB URI: configurable via environment variable `MONGODB_URI`
- Default: `mongodb://localhost:27017`
- Database name: configurable via environment variable `MONGODB_DATABASE` (default: `country-db`)
- Event publication registry will use MongoDB to persist events
- Events are stored in MongoDB and can be replayed on failure

**Retry Configuration**:
- Maximum attempts: 3
- Backoff: 1 second exponential (if issues occur, use fixed 1 second delay)
- Configure via `@ApplicationModuleListener` annotation

**Example Configuration**:
```java
@ApplicationModuleListener
// Retry configuration will be applied through Spring Modulith's event publication registry
public void handleEvent(SomeEvent event) {
    // Implementation
}
```

**Application Properties** (in `src/main/resources/application.yml`):
```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017}
      database: ${MONGODB_DATABASE:country-db}
```

**Dependencies**: Task 1.1

**Acceptance Criteria**:
- MongoDB configuration is in Domain module
- Application connects to MongoDB successfully
- Configuration is externalized via environment variables
- Event publication registry is enabled with MongoDB backend
- Events are persisted to MongoDB and can be retried on failure
- Retry configuration is properly set

---

### Task 2.2: Create Country Entity
**Description**: Create the MongoDB entity for storing country information.

**Requirements**:
- Package: `dev.neate.domain` (or `dev.neate.domain.model`)
- Annotate with `@Document` for MongoDB
- Collection name: `countries`
- Use standard Java class (not record, as MongoDB entities need mutability)

**Entity Fields**:
- `id`: String (use MongoDB auto-generated ObjectId with `@Id` annotation)
- `name`: String (required)
- `code`: String (required, ISO 3166-1 alpha-2)
- `currency`: String (nullable)
- `language`: String (nullable)
- `population`: String (nullable)
- `validCountry`: Boolean (default: false)

**Additional Requirements**:
- Add appropriate getters/setters
- Make entity accessible from other modules (public class)
- Include no-args constructor for MongoDB
- Use `@Id` annotation for MongoDB auto-generated ObjectId

**Example**:
```java
@Document(collection = "countries")
public class Country {
    @Id
    private String id; // MongoDB will auto-generate ObjectId
    // other fields...
}
```

**Dependencies**: Task 2.1

**Acceptance Criteria**:
- Entity is properly annotated for MongoDB
- All fields are correctly typed
- Entity can be persisted to MongoDB

---

### Task 2.3: Create Country Repository
**Description**: Create MongoDB repository for Country entity.

**Requirements**:
- Package: `dev.neate.domain.internal` (internal package for encapsulation)
- Extend `MongoRepository<Country, String>`
- Keep repository package-private (not public)

**Methods Needed**:
- Standard CRUD operations (provided by MongoRepository)
- `findById(String id)`: Optional<Country>
- `save(Country country)`: Country

**Dependencies**: Task 2.2

**Acceptance Criteria**:
- Repository can perform CRUD operations
- Repository is not directly accessible from other modules

---

### Task 2.4: Create CountryService Interface
**Description**: Create the public API interface for domain operations.

**Requirements**:
- Package: `dev.neate.domain` (public API)
- Interface name: `CountryService`

**Methods**:
```java
Country save(Country country);
Optional<Country> findById(String id);
Country update(Country country);
```

**Dependencies**: Task 2.2

**Acceptance Criteria**:
- Interface is public and accessible from other modules
- Method signatures are clear and well-documented

---

### Task 2.5: Implement CountryService
**Description**: Create the implementation of CountryService that uses the repository.

**Requirements**:
- Package: `dev.neate.domain.internal` (internal package)
- Class name: `CountryServiceImpl`
- Annotate with `@Service`
- Inject `CountryRepository`

**Implementation Details**:
- `save()`: Delegates to repository.save()
- `findById()`: Delegates to repository.findById()
- `update()`: Validates entity exists, then saves

**Dependencies**: Task 2.3, Task 2.4

**Acceptance Criteria**:
- Service implementation works correctly
- Repository is encapsulated and not exposed
- All methods handle edge cases appropriately

---

## API Module

### Task 3.1: Create CountryCreatedEvent
**Description**: Create the Spring Modulith event for country creation using Java record.

**Requirements**:
- Package: `dev.neate.api` (public - events are part of module's public API)
- Event class: `CountryCreatedEvent`
- Use Java record for immutability
- Make event public (accessible to other modules)

**Event Structure**:
```java
public record CountryCreatedEvent(String countryId) {}
```

**Dependencies**: None

**Acceptance Criteria**:
- Event is a simple wrapper containing country ID
- Event is immutable
- Event is accessible from other modules

---

### Task 3.2: Create Country Request DTO
**Description**: Create the request DTO for the POST endpoint using Java record.

**Requirements**:
- Package: `dev.neate.api` (public - DTOs are part of module's public API)
- Use Java record: `CreateCountryRequest`
- No validation annotations needed (as per plan)

**DTO Structure**:
```java
public record CreateCountryRequest(
    String name,
    String code
) {}
```

**Dependencies**: None

**Acceptance Criteria**:
- DTO correctly deserializes from JSON
- Fields map to entity fields

---

### Task 3.3: Create Country Controller
**Description**: Create REST controller for country creation endpoint.

**Requirements**:
- Package: `dev.neate.api` (public - controllers are part of module's public API)
- Class name: `CountryController`
- Annotate with `@RestController`
- Inject `CountryService` from domain module
- Inject `ApplicationEventPublisher` for publishing events

**Endpoint**:
- Method: POST
- Path: `/countries`
- Consumes: `application/json`
- Request Body: `CreateCountryRequest`
- Response: 202 Accepted (no body)

**Implementation Logic**:
1. Receive `CreateCountryRequest`
2. Create `Country` entity with:
   - Leave `id` as null (MongoDB will auto-generate)
   - Set name and code from request
   - Set `validCountry` to `false`
   - Leave currency, language, population as null
3. Save entity via `CountryService.save()` (MongoDB generates ID)
4. Publish `CountryCreatedEvent` with the country ID using `ApplicationEventPublisher.publishEvent()`
5. Return `ResponseEntity.accepted().build()`

**Dependencies**: Task 2.4, Task 3.1, Task 3.2

**Acceptance Criteria**:
- Endpoint accepts POST requests at `/countries`
- Country is saved to MongoDB
- Event is published
- Returns 202 status with no body

---

## Validation Module

### Task 4.1: Create CountryValidatedEvent
**Description**: Create the Spring Modulith event for country validation using Java record.

**Requirements**:
- Package: `dev.neate.validation` (public - only events are exposed from this module)
- Event class: `CountryValidatedEvent`
- Use Java record for immutability
- Make event public (accessible to other modules)

**Event Structure**:
```java
public record CountryValidatedEvent(String countryId) {}
```

**Dependencies**: None

**Acceptance Criteria**:
- Event is a simple wrapper containing country ID
- Event is immutable
- Event is accessible from other modules

---

### Task 4.2: Create Validation Service
**Description**: Create service to encapsulate validation logic.

**Requirements**:
- Package: `dev.neate.validation.internal` (internal - not exposed to other modules)
- Class name: `CountryValidationService`
- Annotate with `@Service`

**Validation Logic**:
```java
public boolean validate(Country country) {
    // Check name is not null and not empty
    // Check code is not null and not empty
    // Return true if all checks pass, false otherwise
}
```

**Validation Rules**:
- Name must not be null or empty (after trim)
- Code must not be null or empty (after trim)

**Dependencies**: Task 2.2 (Country entity)

**Acceptance Criteria**:
- Validation logic correctly checks name and code
- Returns true only when all checks pass
- Does not validate currency, language, or population

---

### Task 4.3: Create Validation Event Listener
**Description**: Create event listener to handle CountryCreatedEvent.

**Requirements**:
- Package: `dev.neate.validation.internal` (internal - not exposed to other modules)
- Class name: `CountryCreatedEventListener`
- Annotate with `@Component`
- Inject `CountryService` from domain module
- Inject `CountryValidationService`
- Inject `ApplicationEventPublisher`

**Event Listener Method**:
```java
@ApplicationModuleListener
public void handleCountryCreated(CountryCreatedEvent event) {
    // Implementation
}
```

**Implementation Logic**:
1. Extract country ID from event
2. Fetch country from domain using `CountryService.findById()`
3. If country not found, log error and return
4. Call `CountryValidationService.validate(country)`
5. If validation passes:
   - Set `country.validCountry = true`
   - Save country via `CountryService.save()`
   - Publish `CountryValidatedEvent` with country ID
6. If validation fails:
   - Do NOT save (entity remains with `validCountry=false`)
   - Log validation failure
   - Do NOT publish event

**Dependencies**: Task 3.1, Task 4.1, Task 4.2, Task 2.4

**Acceptance Criteria**:
- Listener responds to CountryCreatedEvent
- Validation logic is executed
- Valid countries are saved with `validCountry=true`
- Invalid countries are not updated
- Event is published only for valid countries

---

## Enrichment Module

### Task 5.1: Create CountryEnrichedEvent
**Description**: Create the Spring Modulith event for country enrichment using Java record.

**Requirements**:
- Package: `dev.neate.enrichment` (public - only events are exposed from this module)
- Event class: `CountryEnrichedEvent`
- Use Java record for immutability
- Make event public (accessible to other modules)

**Event Structure**:
```java
public record CountryEnrichedEvent(String countryId) {}
```

**Dependencies**: None

**Acceptance Criteria**:
- Event is a simple wrapper containing country ID
- Event is immutable
- Event is accessible from other modules

---

### Task 5.2: Create RestCountries API Client
**Description**: Create client to call the restcountries.com API.

**Requirements**:
- Package: `dev.neate.enrichment.internal` (internal - not exposed to other modules)
- Class name: `RestCountriesClient`
- Annotate with `@Component`
- Use `RestClient` for HTTP calls (create using `RestClient.builder()`)
- Add Spring Web dependency to `pom.xml` if not already present
- Configure with sensible default timeout and connection pool settings

**Maven Dependencies**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**API Details**:
- Endpoint: `https://restcountries.com/v3.1/alpha/{code}`
- Method: GET
- Replace `{code}` with country code

**Response Mapping**:
The API returns an array with one object. Extract from the first element:
- `population`: Number at root level (e.g., `69281437`)
- `currencies`: Object with currency codes as keys (e.g., `{"GBP":{"symbol":"£","name":"British pound"}}`)
  - Extract first currency **code** (e.g., "GBP")
- `languages`: Object with language codes as keys (e.g., `{"eng":"English"}`)
  - Extract first language **name** (e.g., "English")

**Example Response Structure** (see `country-response.md` for full example):
```json
[{
  "population": 69281437,
  "currencies": {"GBP": {"symbol": "£", "name": "British pound"}},
  "languages": {"eng": "English"}
}]
```

**Methods**:
```java
public EnrichmentData fetchCountryData(String countryCode) throws EnrichmentException {
    // Call API and return enrichment data
}
```

**DTO for Response** (use Java record):
```java
public record EnrichmentData(
    String population,  // Convert number to String
    String currency,    // Currency code (e.g., "GBP")
    String language     // Language name (e.g., "English")
) {}
```

**RestClient Configuration**:
```java
@Bean
public RestClient restClient() {
    return RestClient.builder()
        .baseUrl("https://restcountries.com/v3.1")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
}
```

**Error Handling**:
- Throw custom `EnrichmentException` on API failure
- Exception will trigger Spring Modulith retry

**Dependencies**: None

**Acceptance Criteria**:
- Client successfully calls restcountries.com API
- Response is correctly parsed
- First values are selected from arrays/objects
- Errors are properly thrown for retry handling

---

### Task 5.3: Create Enrichment Service
**Description**: Create service to orchestrate enrichment logic.

**Requirements**:
- Package: `dev.neate.enrichment.internal` (internal - not exposed to other modules)
- Class name: `CountryEnrichmentService`
- Annotate with `@Service`
- Inject `RestCountriesClient`
- Inject `CountryService` from domain module

**Methods**:
```java
public void enrichCountry(Country country) throws EnrichmentException {
    // Fetch data from API
    // Update country entity
    // Save via CountryService
}
```

**Implementation Logic**:
1. Call `RestCountriesClient.fetchCountryData(country.getCode())`
2. Update country entity:
   - Set `population` from enrichment data
   - Set `currency` from enrichment data
   - Set `language` from enrichment data
3. Save country via `CountryService.save()`

**Dependencies**: Task 5.2, Task 2.4

**Acceptance Criteria**:
- Service correctly enriches country with API data
- Country is saved after enrichment
- Exceptions are propagated for retry handling

---

### Task 5.4: Create Enrichment Event Listener
**Description**: Create event listener to handle CountryValidatedEvent with retry support.

**Requirements**:
- Package: `dev.neate.enrichment.internal` (internal - not exposed to other modules)
- Class name: `CountryValidatedEventListener`
- Annotate with `@Component`
- Inject `CountryService` from domain module
- Inject `CountryEnrichmentService`
- Inject `ApplicationEventPublisher`

**Event Listener Method**:
```java
@ApplicationModuleListener
public void handleCountryValidated(CountryValidatedEvent event) {
    // Implementation
}
```

**Retry Configuration**:
- Use Spring Modulith's retry mechanism
- Maximum attempts: 3
- Backoff: 1 second exponential (if issues occur, use fixed 1 second delay)
- Configure via `@ApplicationModuleListener` annotation

**Implementation Logic**:
1. Extract country ID from event
2. Fetch country from domain using `CountryService.findById()`
3. If country not found, log error and return
4. Check if `country.validCountry == true`
5. If not valid, log warning and return (don't enrich invalid countries)
6. Call `CountryEnrichmentService.enrichCountry(country)`
7. If successful, publish `CountryEnrichedEvent` with country ID
8. If exception thrown, let Spring Modulith retry mechanism handle it

**Dependencies**: Task 4.1, Task 5.1, Task 5.3, Task 2.4

**Acceptance Criteria**:
- Listener responds to CountryValidatedEvent
- Only valid countries are enriched
- Enrichment is performed via external API
- Event is published after successful enrichment
- Retries occur on failure (3 attempts, exponential backoff)

---

## Event Module

### Task 6.1: Configure Kafka in Event Module
**Description**: Set up Kafka configuration within the Event module.

**Requirements**:
- Add Spring Kafka dependency to `pom.xml`
- Create configuration class in `dev.neate.event.internal` package (internal - not exposed)
- Configure Kafka broker connection via environment variables
- Default to localhost for local development
- Configure JSON serialization for Kafka messages

**Maven Dependencies**:
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

**Configuration Details**:
- Kafka bootstrap servers: configurable via environment variable `KAFKA_BOOTSTRAP_SERVERS`
- Default: `localhost:9092`
- Key serializer: StringSerializer
- Value serializer: JsonSerializer

**Application Properties** (in `src/main/resources/application.yml`):
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

**Dependencies**: Task 1.1

**Acceptance Criteria**:
- Kafka configuration is in Event module
- Application connects to Kafka successfully
- Configuration is externalized via environment variables

---

### Task 6.2: Create Kafka Event Producer
**Description**: Create service to produce events to Kafka.

**Requirements**:
- Package: `dev.neate.event.internal` (internal - not exposed to other modules)
- Class name: `CountryKafkaProducer`
- Annotate with `@Service`
- Inject `KafkaTemplate<String, Object>`

**Kafka Configuration**:
- Topic name: `country-events`
- Key: country ID
- Value: JSON object with country data

**Methods**:
```java
public void sendCountryEvent(Country country) throws KafkaException {
    // Send to Kafka
}
```

**Event Payload Structure**:
```json
{
    "id": "country-id",
    "name": "country-name",
    "code": "country-code",
    "currency": "country-currency",
    "language": "country-language",
    "population": "country-population",
    "validCountry": true/false
}
```

**Error Handling**:
- Throw exception on Kafka send failure
- Exception will trigger Spring Modulith retry

**Dependencies**: Task 6.1

**Acceptance Criteria**:
- Producer successfully sends messages to Kafka
- Messages are in correct JSON format
- Errors are properly thrown for retry handling

---

### Task 6.3: Create Event Listener for Country Enriched
**Description**: Create event listener to handle CountryEnrichedEvent and produce to Kafka with retry support.

**Requirements**:
- Package: `dev.neate.event.internal` (internal - not exposed to other modules)
- Class name: `CountryEnrichedEventListener`
- Annotate with `@Component`
- Inject `CountryService` from domain module
- Inject `CountryKafkaProducer`

**Event Listener Method**:
```java
@ApplicationModuleListener
public void handleCountryEnriched(CountryEnrichedEvent event) {
    // Implementation
}
```

**Retry Configuration**:
- Use Spring Modulith's retry mechanism
- Maximum attempts: 3
- Backoff: 1 second exponential (if issues occur, use fixed 1 second delay)
- Configure via `@ApplicationModuleListener` annotation

**Implementation Logic**:
1. Extract country ID from event
2. Fetch country from domain using `CountryService.findById()`
3. If country not found, log error and return
4. Call `CountryKafkaProducer.sendCountryEvent(country)`
5. If successful, log success
6. If exception thrown, let Spring Modulith retry mechanism handle it

**Dependencies**: Task 5.1, Task 6.2, Task 2.4

**Acceptance Criteria**:
- Listener responds to CountryEnrichedEvent
- Country data is sent to Kafka
- Message format matches specification
- Retries occur on failure (3 attempts, exponential backoff)

---

## Testing Tasks

### Task 7.1: Integration Test - Complete Flow
**Description**: Create integration test to verify the complete event flow.

**Requirements**:
- Use JUnit 5 as test framework
- Annotate test class with `@SpringBootTest`
- Use TestContainers for MongoDB and Kafka
- Use `MockRestServiceServer` to mock restcountries.com API
- Use SLF4J for logging (Spring Boot default)

**Maven Dependencies**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mongodb</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>kafka</artifactId>
    <scope>test</scope>
</dependency>
```

**Test Scenario**:
1. Start TestContainers for MongoDB and Kafka
2. Mock restcountries.com API using `MockRestServiceServer`
3. POST request to `/countries` with valid data
4. Verify country is saved to MongoDB
5. Verify validation occurs and `validCountry` is set to true
6. Verify enrichment occurs and fields are populated
7. Verify event is produced to Kafka

**Dependencies**: All module tasks completed

**Acceptance Criteria**:
- End-to-end flow works correctly
- All events are processed in order
- Final country entity has all fields populated
- Kafka message is produced
- TestContainers properly start and stop

---

### Task 7.2: Integration Test - Validation Failure
**Description**: Test that invalid countries are not enriched or sent to Kafka.

**Requirements**:
- Use JUnit 5 and `@SpringBootTest`
- Use TestContainers for MongoDB and Kafka

**Test Scenario**:
1. Start TestContainers for MongoDB and Kafka
2. POST request to `/countries` with empty name
3. Verify country is saved with `validCountry=false`
4. Verify no enrichment occurs
5. Verify no Kafka event is produced

**Dependencies**: All module tasks completed

**Acceptance Criteria**:
- Invalid countries remain with `validCountry=false`
- Event chain stops after validation failure

---

### Task 7.3: Integration Test - Enrichment Retry
**Description**: Test that enrichment retries on API failure.

**Requirements**:
- Use JUnit 5 and `@SpringBootTest`
- Use TestContainers for MongoDB and Kafka
- Use `MockRestServiceServer` to simulate API failures

**Test Scenario**:
1. Start TestContainers for MongoDB and Kafka
2. Configure `MockRestServiceServer` to fail initially, then succeed
3. POST request to `/countries` with valid data
4. Verify retry mechanism attempts enrichment 3 times
5. Verify backoff is applied (exponential or fixed 1 second)

**Dependencies**: All module tasks completed

**Acceptance Criteria**:
- Retry mechanism works as configured
- Backoff strategy is applied correctly
- Enrichment eventually succeeds after retries

---

## Summary

**Total Tasks**: 25 tasks across 7 modules

**Build Tool**: Maven

**Java Version**: 21

**Key Decisions**:
- Use Java records for immutable data structures (events, DTOs, value objects)
- MongoDB configuration managed by Domain module (includes Spring Modulith event publication)
- Kafka configuration managed by Event module
- Each module manages its own infrastructure dependencies
- Spring Modulith BOM for dependency management
- Proper module isolation using `internal` packages:
  - **API Module**: Public - events, DTOs, controllers
  - **Domain Module**: Public - entity, service interface | Internal - repository, service impl
  - **Validation Module**: Public - events only | Internal - services, listeners
  - **Enrichment Module**: Public - events only | Internal - clients, services, listeners
  - **Event Module**: Public - none | Internal - all components

**Recommended Implementation Order**:
1. Application Module (Task 1.1)
2. Domain Module (Tasks 2.1-2.5) - includes MongoDB and event publication setup
3. API Module (Tasks 3.1-3.3)
4. Validation Module (Tasks 4.1-4.3)
5. Enrichment Module (Tasks 5.1-5.4)
6. Event Module (Tasks 6.1-6.3)
7. Testing (Tasks 7.1-7.3)

**Key Dependencies**:
- Domain module must be completed before other modules
- Events should be created before their listeners
- Testing should be done after all modules are complete
