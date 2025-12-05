package dev.neate.domain.internal;

import dev.neate.domain.Country;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

/**
 * MongoDB repository for Country entities.
 * 
 * This repository is internal to the Domain module and not exposed to other modules.
 * Access to countries should be through the CountryService interface.
 * - count(): Count all countries
 */
interface CountryRepository extends MongoRepository<Country, UUID> {
    // Standard CRUD operations are provided by MongoRepository
    // Additional custom query methods can be added here if needed
}
