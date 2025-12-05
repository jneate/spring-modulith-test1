package dev.neate.domain.internal;

import dev.neate.domain.Country;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * MongoDB repository for Country entity.
 * 
 * This repository is package-private (internal) and not directly accessible
 * from other modules. Access to the repository is provided through the
 * CountryService interface, which is part of the Domain module's public API.
 * 
 * Extends MongoRepository to provide standard CRUD operations:
 * - save(Country): Save or update a country
 * - findById(String): Find a country by ID
 * - findAll(): Find all countries
 * - delete(Country): Delete a country
 * - count(): Count all countries
 */
interface CountryRepository extends MongoRepository<Country, String> {
    // Standard CRUD operations are provided by MongoRepository
    // Additional custom query methods can be added here if needed
}
