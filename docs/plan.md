# Spring Modulith Test 1 Plan

The application should be developed in Java 21, using the latest stable version of Spring Modulith.

The application should follow basic coding principles and best practices, such as SOLID, DRY, YAGNI, and KISS.

The base package for the application should be `dev.neate` and follow Spring Modulith best practices for package structure.

The build tool should be Maven.

Use Java records where suitable for immutable data structures (DTOs, events, value objects).

## Configuration

Each module should manage its own infrastructure configuration following Spring Modulith best practices.

### MongoDB Configuration (Domain Module)

MongoDB configuration should be managed by the Domain module.
- Configuration provided via environment variables
- For local development, assume MongoDB is running on localhost with default settings
- Default connection: `mongodb://localhost:27017`
- Database name: configurable (e.g., `country-db`)

### Kafka Configuration (Event Module)

Kafka configuration should be managed by the Event module.
- Configuration provided via environment variables
- For local development, assume Kafka is running on localhost with default settings
- Default bootstrap servers: `localhost:9092`

### Spring Modulith Event Publication (Application Module)

Spring Modulith event publication registry should be configured to use MongoDB as the backend.
- Use `spring-modulith-starter-mongodb` dependency
- Events are persisted to MongoDB for reliability and replay capability
- Retry configuration: 3 attempts with 1 second exponential backoff

## Event Flow

The application follows an event-driven architecture using Spring Modulith events. The flow is as follows:

1. **API Module** → Creates country entity → Saves to MongoDB → Emits `CountryCreatedEvent`
2. **Validation Module** → Listens to `CountryCreatedEvent` → Validates country → Saves if valid → Emits `CountryValidatedEvent`
3. **Enrichment Module** → Listens to `CountryValidatedEvent` → Enriches with external API data → Saves enriched entity → Emits `CountryEnrichedEvent`
4. **Event Module** → Listens to `CountryEnrichedEvent` → Produces to Kafka topic `country-events`

### Event Types

- **CountryCreatedEvent**: Simple wrapper containing country ID (String)
  - Published by: API Module
  - Consumed by: Validation Module

- **CountryValidatedEvent**: Simple wrapper containing country ID (String)
  - Published by: Validation Module
  - Consumed by: Enrichment Module

- **CountryEnrichedEvent**: Simple wrapper containing country ID (String)
  - Published by: Enrichment Module
  - Consumed by: Event Module

All Spring Modulith events are asynchronous and follow the event publication registry pattern for reliability.

## Modules

The application will contain 6 modules:

- `application`: The main application module
- `api`: The REST API module for country creation
- `domain`: The domain module, this includes MongoDB entities, repositories, and service interface
- `validation`: The validation module, this includes validation logic
- `enrichment`: The enrichment module, this includes enrichment logic which calls a public API to fetch additional data
- `event`: The event module, this includes event production logic to Kafka

### Application Module

The application module should contain the main application class and the entry point for the application.

### API Module

The API module should expose a REST API for creating countries.

The API should provide a `POST /countries` endpoint that accepts JSON with the following fields:

- `name`: The name of the country
- `code`: The ISO 3166-1 alpha-2 code of the country

Example request:
```json
{
  "name": "United Kingdom",
  "code": "GB"
}
```

On successful creation, the API should:

1. Create a country entity with `validCountry` set to `false` initially
2. Save the country entity to MongoDB via the Domain module's `CountryService`
3. Emit a Spring Modulith `CountryCreatedEvent` containing the ID of the created entity
4. Return a 202 Accepted status with no response body

The API module should only handle country creation. Query and retrieval endpoints are not required.

API-level validation is not required at this stage.

### Domain Module

The domain module should contain the domain entities, MongoDB repositories, and a service interface.

The domain entities/types should be accessible from other modules.

The domain module should expose a `CountryService` interface as part of its public API. This service should provide methods for:

- Saving countries
- Finding countries by ID
- Updating countries

The MongoDB repositories should remain internal to the Domain module. Other modules should only interact with the domain through the `CountryService` interface.

The domain entity will contain country information and have the following fields:

- `id`: The ID of the country, random v4 uuid (String)
- `name`: The name of the country (String, required)
- `code`: The ISO 3166-1 alpha-2 code of the country (String, required)
- `currency`: The currency of the country (String, nullable, populated by enrichment)
- `language`: The language of the country (String, nullable, populated by enrichment)
- `population`: The population of the country (String, nullable, populated by enrichment)
- `validCountry`: This is a flag that the validation module will set to true if the country is valid and passes the validation logic checks (Boolean, initially `false`)

The domain entities will be persisted in MongoDB.

The `currency`, `language`, and `population` fields should be nullable/optional initially, as they will be populated by the Enrichment module.

### Validation Module

The validation module should listen for `CountryCreatedEvent` from the API module.

The validation module should contain the validation logic, the validation logic is a set of business rules that are applied to the domain entities.

The validation module should fetch the country entity from the domain using the entity ID in the event via the `CountryService`.

Business rules for country validation:

- The name of the country should not be empty
- The code of the country should not be empty

Note: The `currency`, `language`, and `population` fields are not validated as they will be populated later by the Enrichment module.

Once all of the validation checks have been applied on an entity, the validation module should set the `validCountry` flag to true.

If any of the checks fail, the validation module should NOT save the entity (it remains with `validCountry=false` as initially set).

Only when validation passes should the entity be saved via the `CountryService` with `validCountry=true`, and then emit a Spring Modulith `CountryValidatedEvent` containing the ID of the entity that has been validated.

The `CountryValidatedEvent` should be a simple wrapper containing the country ID (String).

### Enrichment Module

The enrichment module should listen for `CountryValidatedEvent` from the Validation Module.

The enrichment module should fetch the entity from the domain using the entity ID in the event via the `CountryService`.

The enrichment module should only process countries where `validCountry` is true. Invalid countries should not be enriched.

The enrichment module should contain the enrichment logic, the enrichment logic calls a public API to fetch additional data.

The enrichment module should use `RestClient` to make HTTP calls.

The enrichment module should enrich the domain entities with additional data from the `https://restcountries.com/v3.1/alpha/{code}` API.

Replacing the `{code}` with the code of the country from the domain entity.

The enrichment module should enrich the domain entities to set the following fields:

- `population`
- `currency` (select the first currency if multiple are returned)
- `language` (select the first language if multiple are returned)

If the enrichment API call fails, the module should use Spring Modulith's built-in retry functionality to retry the operation.

The retry strategy should be configured as follows:
- Maximum of 3 retry attempts
- 1 second exponential backoff between retries
- Use Spring Modulith's internal event publication retry mechanism

After successful enrichment, the entity should be saved via the `CountryService` and the enrichment module should emit a Spring Modulith `CountryEnrichedEvent` containing the ID of the entity that has been enriched.

The `CountryEnrichedEvent` should be a simple wrapper containing the country ID (String).

### Event Module

The event module should listen for `CountryEnrichedEvent` from the Enrichment Module.

The event module should fetch the entity from the domain using the entity ID in the event via the `CountryService`.

The event module should contain the event production logic.

Note: The `CountryCreatedEvent` emitted by the API module should also be a simple wrapper containing the country ID (String).

The event module should produce events to Kafka when a country has been successfully created, validated, and enriched.

The events should be produced to the `country-events` topic.

If the Kafka event production fails, the module should use Spring Modulith's built-in retry functionality to retry the operation.

The retry strategy should be configured as follows:
- Maximum of 3 retry attempts
- 1 second exponential backoff between retries
- Use Spring Modulith's internal event publication retry mechanism

The events should be produced as JSON objects with the following structure:

```json
{
    "id": "<country-id>",
    "name": "<country-name>",
    "code": "<country-code>",
    "currency": "<country-currency>",
    "language": "<country-language>",
    "population": <country-population>,
    "validCountry": <country-valid-country>
}
```
