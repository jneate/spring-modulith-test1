package dev.neate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for the main Application.
 * 
 * Verifies that:
 * - The Spring Boot application context loads successfully
 * - Spring Modulith detects all expected modules
 * - Module structure follows Spring Modulith conventions
 */
@SpringBootTest
class ApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
    }

    @Test
    void verifiesModularStructure() {
        // Verify Spring Modulith module structure
        ApplicationModules modules = ApplicationModules.of(Application.class);
        
        // Verify all expected modules are detected
        // Note: Spring Modulith 2.0.0 detects internal packages as separate modules
        assertThat(modules.stream()
                .map(module -> module.getName())
                .toList())
            .containsExactlyInAnyOrder(
                "api",
                "domain",
                "domain.internal",
                "validation",
                "validation.internal",
                "enrichment",
                "enrichment.internal",
                "event",
                "event.internal"
            );
        
        // Verify module structure is valid
        modules.verify();
    }

    @Test
    void generatesModuleDocumentation() {
        // Generate module documentation
        ApplicationModules modules = ApplicationModules.of(Application.class);
        
        // This generates PlantUML diagrams and documentation
        new Documenter(modules)
            .writeDocumentation()
            .writeIndividualModulesAsPlantUml();
    }
}
