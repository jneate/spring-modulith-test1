package dev.neate.domain.internal;

import dev.neate.domain.Country;
import dev.neate.domain.CountryService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of CountryService.
 * 
 * This implementation is package-private (internal) and encapsulates
 * the CountryRepository. Other modules access country operations through
 * the public CountryService interface.
 * 
 * All operations delegate to the repository with appropriate validation
 * and error handling.
 */
@Service
class CountryServiceImpl implements CountryService {

    private final CountryRepository repository;

    /**
     * Constructor injection of the repository.
     *
     * @param repository the country repository
     */
    public CountryServiceImpl(CountryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Country save(Country country) {
        // Generate UUID for new countries
        if (country.getId() == null) {
            country.setId(UUID.randomUUID());
        }
        return repository.save(country);
    }

    @Override
    public Optional<Country> findById(UUID id) {
        return repository.findById(id);
    }
}
