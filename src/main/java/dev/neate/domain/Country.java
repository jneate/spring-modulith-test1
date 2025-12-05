package dev.neate.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * MongoDB entity representing a country.
 * 
 * This entity stores country information including basic details,
 * enrichment data, and validation status. It is part of the Domain module's
 * public API and can be accessed by other modules.
 * 
 * The entity uses UUID for the primary key for consistent identification
 * across distributed systems.
 */
@Document(collection = "countries")
public class Country {

    @Id
    private UUID id; // UUID for unique identification

    private String name;
    private String code; // ISO 3166-1 alpha-2 code
    private String currency;
    private String language;
    private String population;
    private Boolean validCountry;

    /**
     * No-args constructor required by MongoDB.
     */
    public Country() {
        this.validCountry = false; // Default value
    }

    /**
     * Constructor with required fields.
     *
     * @param name the country name
     * @param code the ISO 3166-1 alpha-2 country code
     */
    public Country(String name, String code) {
        this.name = name;
        this.code = code;
        this.validCountry = false;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPopulation() {
        return population;
    }

    public void setPopulation(String population) {
        this.population = population;
    }

    public Boolean getValidCountry() {
        return validCountry;
    }

    public void setValidCountry(Boolean validCountry) {
        this.validCountry = validCountry;
    }

    @Override
    public String toString() {
        return "Country{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", currency='" + currency + '\'' +
                ", language='" + language + '\'' +
                ", population='" + population + '\'' +
                ", validCountry=" + validCountry +
                '}';
    }
}
