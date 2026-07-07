package dev.ronin.demo.beerstore.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import dev.ronin.demo.beerstore.BeerStoreApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Verifies the module structure declared via the {@code package-info.java} files under
 * {@code customer}, {@code order}, {@code product} and {@code shared} - in particular
 * that {@code customer}/{@code product} depend on nothing but the always-open {@code shared}
 * module, and {@code order} depends only on {@code customer}/{@code product} (plus
 * {@code shared}). {@code platform} is deliberately excluded from the scan below: it's a
 * cross-cutting technical foundation, not a business domain, so Spring Modulith should only
 * validate the actual bounded contexts - the boundary invariants Modulith would otherwise have
 * enforced for {@code platform} (no dependency back onto a business module; {@code openapi}/
 * {@code ws} never accessed from outside) are instead ArchUnit rules in
 * {@code PlatformBoundaryTest}.
 * Scoped to main classes only (like the rest of this ArchUnit suite) so that test-only packages
 * such as {@code contract} don't get swept in as accidental extra modules.
 */
class ModularityTests {

    private static final ImportOption DO_NOT_INCLUDE_TESTS = new ImportOption.DoNotIncludeTests();

    ApplicationModules modules = ApplicationModules.of(BeerStoreApplication.class,
            (ImportOption) location -> DO_NOT_INCLUDE_TESTS.includes(location)
                    && !location.contains("dev/ronin/demo/beerstore/platform/"));

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
