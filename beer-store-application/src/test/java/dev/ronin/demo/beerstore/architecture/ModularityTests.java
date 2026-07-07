package dev.ronin.demo.beerstore.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import dev.ronin.demo.beerstore.BeerStoreApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Verifies the module structure declared via the {@code package-info.java} files under
 * {@code customer}, {@code order}, {@code shared} and {@code infrastructure} - in particular
 * that {@code customer} depends on nothing else and {@code order} depends only on
 * {@code customer} (plus the always-open {@code shared}/{@code infrastructure} modules).
 * Scoped to main classes only (like the rest of this ArchUnit suite) so that test-only packages
 * such as {@code contract} don't get swept in as accidental extra modules.
 */
class ModularityTests {

    ApplicationModules modules = ApplicationModules.of(BeerStoreApplication.class, new ImportOption.DoNotIncludeTests());

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
