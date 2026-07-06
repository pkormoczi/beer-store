package dev.ronin.demo.beerstore.domain.achitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import dev.ronin.demo.beerstore.BeerStoreApplication;
import dev.ronin.demo.beerstore.infrastructure.logging.PropertiesLogger;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "dev.ronin.demo.beerstore",
        importOptions = {
                ImportOption.DoNotIncludeTests.class,
                ImportOption.DoNotIncludeArchives.class,
                ImportOption.DoNotIncludeJars.class})
@SuppressWarnings("squid:S2187")
public class ApplicationArchitectureTest {

    @ArchTest
    public static final ArchRule layering =
            layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Domain").definedBy("..domain..")
                    .layer("Adapter").definedBy("..adapter..")
                    .layer("Infrastructure").definedBy("..infrastructure..")
                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("Adapter", "Infrastructure")
                    .whereLayer("Adapter").mayOnlyBeAccessedByLayers("Infrastructure")
                    .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Adapter")
                    // The composition root wires a cross-cutting listener directly; it sits
                    // outside all three layers by design and is not itself a layering concern.
                    .ignoreDependency(BeerStoreApplication.class, PropertiesLogger.class);

}
