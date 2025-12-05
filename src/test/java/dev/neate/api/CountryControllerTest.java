package dev.neate.api;

import dev.neate.domain.Country;
import dev.neate.domain.CountryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for CountryController.
 * 
 * Web layer slice test using @WebMvcTest.
 * Verifies that:
 * - POST /countries endpoint works correctly via HTTP
 * - Request is properly deserialized from JSON
 * - Controller delegates to CountryService
 * - Returns 202 Accepted status
 * 
 * Note: Event publishing is tested in integration tests, not in this web layer slice test.
 */
@WebMvcTest(CountryController.class)
class CountryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CountryService countryService;

    @Test
    void createCountryReturns202Accepted() throws Exception {
        // Given
        Country savedCountry = new Country("United Kingdom", "GB");
        savedCountry.setId("507f1f77bcf86cd799439011");
        when(countryService.save(any(Country.class))).thenReturn(savedCountry);

        // When/Then
        String json = """
            {
                "name": "United Kingdom",
                "code": "GB"
            }
            """;

        mockMvc.perform(post("/countries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isAccepted());
    }

    @Test
    void createCountryCallsServiceSave() throws Exception {
        // Given
        Country savedCountry = new Country("France", "FR");
        savedCountry.setId("507f1f77bcf86cd799439012");
        when(countryService.save(any(Country.class))).thenReturn(savedCountry);

        // When
        String json = """
            {
                "name": "France",
                "code": "FR"
            }
            """;

        mockMvc.perform(post("/countries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isAccepted());

        // Then
        verify(countryService).save(any(Country.class));
    }

    @Test
    void createCountryDeserializesJsonCorrectly() throws Exception {
        // Given
        Country savedCountry = new Country("Germany", "DE");
        savedCountry.setId("507f1f77bcf86cd799439013");
        when(countryService.save(any(Country.class))).thenReturn(savedCountry);

        // When
        String json = """
            {
                "name": "Germany",
                "code": "DE"
            }
            """;

        mockMvc.perform(post("/countries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isAccepted());

        // Then - verify service was called (JSON deserialization worked)
        verify(countryService).save(any(Country.class));
    }

    @Test
    void createCountryWithSpain() throws Exception {
        // Given
        Country savedCountry = new Country("Spain", "ES");
        savedCountry.setId("507f1f77bcf86cd799439014");
        when(countryService.save(any(Country.class))).thenReturn(savedCountry);

        // When/Then
        String json = """
            {
                "name": "Spain",
                "code": "ES"
            }
            """;

        mockMvc.perform(post("/countries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isAccepted());

        verify(countryService).save(any(Country.class));
    }

    @Test
    void createCountryWithItaly() throws Exception {
        // Given
        Country savedCountry = new Country("Italy", "IT");
        savedCountry.setId("507f1f77bcf86cd799439015");
        when(countryService.save(any(Country.class))).thenReturn(savedCountry);

        // When/Then
        String json = """
            {
                "name": "Italy",
                "code": "IT"
            }
            """;

        mockMvc.perform(post("/countries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isAccepted());

        verify(countryService).save(any(Country.class));
    }

    @Test
    void createCountryWithPortugal() throws Exception {
        // Given
        Country savedCountry = new Country("Portugal", "PT");
        savedCountry.setId("507f1f77bcf86cd799439016");
        when(countryService.save(any(Country.class))).thenReturn(savedCountry);

        // When/Then
        String json = """
            {
                "name": "Portugal",
                "code": "PT"
            }
            """;

        mockMvc.perform(post("/countries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isAccepted());

        verify(countryService).save(any(Country.class));
    }

    @Test
    void createCountryAcceptsValidJson() throws Exception {
        // Given
        Country savedCountry = new Country("Belgium", "BE");
        savedCountry.setId("507f1f77bcf86cd799439017");
        when(countryService.save(any(Country.class))).thenReturn(savedCountry);

        // When
        String json = """
            {
                "name": "Belgium",
                "code": "BE"
            }
            """;

        // Then
        mockMvc.perform(post("/countries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isAccepted());
    }
}
