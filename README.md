# Spring Modulith Test 1

A Spring Boot 4.0.0 application demonstrating modular architecture using Spring Modulith with event-driven communication between modules.

## Architecture

The application is organized into the following modules:

- **API Module** (`dev.neate.api`) - REST endpoints for external clients
- **Domain Module** (`dev.neate.domain`) - Core business logic and data persistence
- **Validation Module** (`dev.neate.validation`) - Country validation logic
- **Enrichment Module** (`dev.neate.enrichment`) - Country data enrichment (planned)

## Features

- ✅ REST API for creating countries
- ✅ MongoDB persistence with Spring Data
- ✅ Event-driven architecture using Spring Modulith
- ✅ Automatic country validation via event listeners
- ✅ Modular design with clear boundaries
- ✅ Comprehensive test coverage (72 tests)

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker (for running MongoDB)

## Running the Application Locally

### 1. Start MongoDB

The application requires MongoDB configured as a **replica set** to support Spring Modulith's event publication feature (which uses transactions).

```bash
docker network create mongoCluster

docker run -d --rm -p 27017:27017 --name mongo1 --network mongoCluster mongo:7.0 mongod --replSet rs0 --bind_ip localhost,mongo1

docker run -d --rm -p 27018:27017 --name mongo2 --network mongoCluster mongo:7.0 mongod --replSet rs0 --bind_ip localhost,mongo2

docker run -d --rm -p 27019:27017 --name mongo3 --network mongoCluster mongo:7.0 mongod --replSet rs0 --bind_ip localhost,mongo3

docker exec -it mongo1 mongosh --eval "rs.initiate({
 _id: \"rs0\",
 members: [
   {_id: 0, host: \"mongo1\"},
   {_id: 1, host: \"mongo2\"},
   {_id: 2, host: \"mongo3\"}
 ]
})"

# Verify the replica set:
docker exec -it mongo1 mongosh --eval "rs.status()"
```

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

- `MONGODB_URI` - MongoDB connection URI (default: `mongodb://localhost:27017`)
- `MONGODB_DATABASE` - Database name (default: `country-db`)

Example:

```bash
MONGODB_URI=mongodb://localhost:27017 MONGODB_DATABASE=my-db mvn spring-boot:run
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

**Note:** Tests use Testcontainers to automatically start MongoDB, so Docker must be running.

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
6. If invalid: Country remains with `validCountry=false`

**Events Published:**
- `CountryCreatedEvent` - Contains the ID of the newly created country
- `CountryValidatedEvent` - Published only if validation passes

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
│           └── Application.java
└── test/
    └── java/
        └── dev/neate/
            ├── api/              # API layer tests
            ├── domain/           # Domain layer tests
            ├── validation/       # Validation layer tests
            └── TestcontainersConfiguration.java
```

### Spring Boot 4.0.0 Notes

This project uses Spring Boot 4.0.0 which has updated package structures:

- `@WebMvcTest`: `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`
- `@MockitoBean`: `org.springframework.test.context.bean.override.mockito.MockitoBean`
- Spring Data MongoDB: `org.springframework.data.mongodb.*`

## Stopping the Application

1. Stop the Spring Boot application: `Ctrl+C`
2. Stop and remove the MongoDB container:
   ```bash
   docker stop mongodb
   docker rm mongodb
   ```

## Documentation

- [Tasks](docs/tasks.md) - Detailed task breakdown
- [Tracker](docs/tracker.md) - Implementation progress tracking
