package dev.neate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Spring Modulith Test 1.
 * 
 * This application demonstrates a modular, event-driven architecture using Spring Modulith.
 * The application is organized into the following modules:
 * - api: REST API for country creation
 * - domain: Core domain entities and services
 * - validation: Business rule validation
 * - enrichment: External API data enrichment
 * - event: Kafka event production
 * 
 * Spring Modulith automatically detects and validates module boundaries based on package structure.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
