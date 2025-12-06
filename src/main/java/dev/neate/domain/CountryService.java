package dev.neate.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Public service interface for Country domain operations.
 * 
 * This interface is part of the Domain module's public API and can be
 * accessed by other modules. The implementation is internal and encapsulates
 * the repository, ensuring proper separation of concerns.
 * 
 * All operations are transactional and handle entity lifecycle management.
 */
public interface CountryService {

    /**
     * Save a new country or update an existing one.
     *
     * @param country the country to save
     * @return the saved country with generated ID (if new)
     */
    Country save(Country country);

    /**
     * Find a country by its ID.
     *
     * @param id the country ID (UUID)
     * @return an Optional containing the country if found, empty otherwise
     */
    Optional<Country> findById(UUID id);
}
