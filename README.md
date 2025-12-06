# Spring Modulith Test 1

A Spring Boot 4.0.0 application demonstrating modular architecture using Spring Modulith with event-driven communication between modules.

## Architecture

The application is organized into the following modules:

- **API Module** (`dev.neate.api`) - REST endpoints for external clients
- **Domain Module** (`dev.neate.domain`) - Core business logic and data persistence
- **Validation Module** (`dev.neate.validation`) - Country validation logic
- **Enrichment Module** (`dev.neate.enrichment`) - Country data enrichment via external APIs
- **Event Module** (`dev.neate.event`) - Kafka event production

## Features

- ✅ REST API for creating countries
- ✅ MongoDB persistence with Spring Data
- ✅ Event-driven architecture using Spring Modulith
- ✅ Automatic country validation via event listeners
- ✅ Country data enrichment from RestCountries API
- ✅ Kafka event production for enriched countries
- ✅ Modular design with clear boundaries
- ✅ Comprehensive test coverage (118 tests)

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker (for running MongoDB and Kafka)

## Running the Application Locally

### 1. Start backing services

> Note: The application requires MongoDB configured as a **replica set** to support Spring Modulith's event publication feature (which uses transactions). To support this I had to add the following to my hosts file in order for the application to resolve the hostnames of the MongoDB replica set, you can also do this via assigning each container a static IP address. See https://www.mongodb.com/community/forums/t/mongodb-replica-docker-cannot-connect-on-replica-only-individual-connection/12802/5 for more information.

```text
127.0.10.1 mongo1
127.0.10.2 mongo2
127.0.10.3 mongo3
```

To bring up the application dependencies:

```sh
docker-compose up -d
```

**Note:** Tests use Testcontainers which automatically start Kafka and Mongo, so this is only needed for local development.

### 2. Start the Application

Run the Spring Boot application using Maven:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Test the API

Create a country using the REST API:

**PowerShell:**
```powershell
Invoke-WebRequest -Uri http://localhost:8080/countries -Method POST -Headers @{"Content-Type"="application/json"} -Body '{"name": "United Kingdom", "code": "GB"}'
```

**Bash/curl:**
```bash
curl -X POST http://localhost:8080/countries \
  -H "Content-Type: application/json" \
  -d '{"name": "United Kingdom", "code": "GB"}'
```

Expected response: `202 Accepted`

### 4. Verify Data in MongoDB

Check the saved countries in MongoDB:

```bash
docker exec mongodb mongosh country-db --quiet --eval "db.countries.find().pretty()"
```

## Configuration

The application can be configured via environment variables:

- `MONGODB_URI` - MongoDB connection URI (default: `mongodb://localhost:27017/?replicaSet=rs0`)
- `MONGODB_DATABASE` - Database name (default: `country-db`)
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka bootstrap servers (default: `localhost:9092`)

Example:

```bash
MONGODB_URI=mongodb://localhost:27017/?replicaSet=rs0 KAFKA_BOOTSTRAP_SERVERS=localhost:9092 mvn spring-boot:run
```

## Running Tests

Run all tests:

```bash
mvn test
```

Run a specific test class:

```bash
mvn test -Dtest=CountryControllerTest
```

**Note:** Tests use Testcontainers to automatically start MongoDB and Kafka, so Docker must be running.

## API Endpoints

### Create Country

**POST** `/countries`

Creates a new country and triggers asynchronous validation.

**Request Body:**
```json
{
  "name": "United Kingdom",
  "code": "GB"
}
```

**Response:** `202 Accepted` (no body)

**Process Flow:**
1. Country is created with `validCountry=false`
2. `CountryCreatedEvent` is published
3. Validation module listens to the event
4. Country is validated (name and code must not be null/empty)
5. If valid: `validCountry` is set to `true` and `CountryValidatedEvent` is published
6. Enrichment module listens to `CountryValidatedEvent`
7. Country data is enriched from RestCountries API (population, currency, language)
8. `CountryEnrichedEvent` is published
9. Event module listens to `CountryEnrichedEvent`
10. Enriched country data is sent to Kafka topic `country-events`

**Events Published:**
- `CountryCreatedEvent` - Contains the ID of the newly created country
- `CountryValidatedEvent` - Published only if validation passes
- `CountryEnrichedEvent` - Published after successful enrichment

**Kafka Topics:**
- `country-events` - Enriched country data in JSON format

## Development

### Project Structure

```
src/
├── main/
│   └── java/
│       └── dev/neate/
│           ├── api/              # REST controllers and DTOs
│           ├── domain/           # Domain entities and services
│           │   └── internal/     # Internal implementation details
│           ├── validation/       # Validation events and logic
│           │   └── internal/     # Internal validation services
│           ├── enrichment/       # Enrichment events and logic
│           │   └── internal/     # Internal enrichment services
│           ├── event/            # Kafka event production
│           │   └── internal/     # Internal Kafka producers
│           └── Application.java
└── test/
    └── java/
        └── dev/neate/
            ├── api/              # API layer tests
            ├── domain/           # Domain layer tests
            ├── validation/       # Validation layer tests
            ├── enrichment/       # Enrichment layer tests
            ├── event/            # Event layer tests
            └── TestcontainersConfiguration.java
```

### Spring Boot 4.0.0 Notes

This project uses Spring Boot 4.0.0 which has updated package structures:

- `@WebMvcTest`: `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`
- `@MockitoBean`: `org.springframework.test.context.bean.override.mockito.MockitoBean`
- Spring Data MongoDB: `org.springframework.data.mongodb.*`

## Stopping the Application

1. Stop the Spring Boot application: `Ctrl+C`
2. Stop and remove the containers:
   ```bash
   # Stop Kafka
   docker stop kafka
   
   # Stop MongoDB replica set
   docker stop mongo1 mongo2 mongo3
   
   # Remove network
   docker network rm mongoCluster
   ```

## Documentation

- [Tasks](docs/tasks.md) - Detailed task breakdown
- [Tracker](docs/tracker.md) - Implementation progress tracking
