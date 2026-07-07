package dev.ronin.demo.beerstore.architecture;

import dev.ronin.demo.beerstore.BeerStoreApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Verifies the module structure declared via the {@code package-info.java} files under
 * {@code customer}, {@code order}, {@code product} and {@code shared} - in particular
 * that {@code customer}/{@code product} depend on nothing but the always-open {@code shared}
 * module, and {@code order} depends only on {@code customer}/{@code product} (plus
 * {@code shared}). {@code platform} never becomes a Spring Modulith module in the first place:
 * {@code spring.modulith.detection-strategy=explicitly-annotated} (see application.yml) means only
 * packages carrying {@code @ApplicationModule} are considered, and {@code platform} deliberately
 * carries none - it's a cross-cutting technical foundation, not a business domain. The boundary
 * invariants Modulith would otherwise have enforced for {@code platform} (no dependency back onto
 * a business module; {@code openapi}/{@code ws} never accessed from outside) are instead ArchUnit
 * rules in {@code PlatformBoundaryTest}.
 */
class ModularityTests {

    ApplicationModules modules = ApplicationModules.of(BeerStoreApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void writesDocumentation() {
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}
