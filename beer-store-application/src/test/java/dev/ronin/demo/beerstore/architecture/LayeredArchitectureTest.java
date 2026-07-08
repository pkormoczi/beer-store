package dev.ronin.demo.beerstore.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static dev.ronin.demo.beerstore.architecture.ArchitectureSupport.APPLICATION_PACKAGE;
import static dev.ronin.demo.beerstore.architecture.ArchitectureSupport.DOMAIN_PACKAGE;

/**
 * Enforces the "onion" layering inside every module's own package tree - {@code domain} must
 * stay innermost and {@code application} must not reach into {@code adapter} - which {@link
 * ModularityTests}' Spring Modulith {@code ApplicationModules.verify()} does not check on its
 * own, since Modulith only guards the boundaries *between* modules, not the layering *within*
 * one. The one documented exception is {@code domain} depending on a module's own {@code
 * api.type} value types (e.g. {@code Address}/{@code OrderStatus}/{@code BeerStyle}, reused by
 * both the DTOs and the internal aggregate) - a deliberate choice recorded in {@code CLAUDE.md},
 * so no rule here forbids {@code domain} from depending on {@code api}.
 */
@AnalyzeClasses(packages = "dev.ronin.demo.beerstore",
        importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeArchives.class, ImportOption.DoNotIncludeJars.class})
@SuppressWarnings("squid:S2187")
class LayeredArchitectureTest {

    private static final String ADAPTER_PACKAGE = "..adapter..";

    @ArchTest
    public static final ArchRule domainShouldNotDependOnApplicationOrAdapter =
            noClasses().that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat().resideInAnyPackage(APPLICATION_PACKAGE, ADAPTER_PACKAGE)
                    .because("the domain model is the innermost layer - it must never know about the "
                            + "orchestration layer above it or any adapter technology");

    @ArchTest
    public static final ArchRule applicationShouldNotDependOnAdapter =
            noClasses().that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(ADAPTER_PACKAGE)
                    .because("application services depend on their own outbound ports, never on a "
                            + "concrete adapter implementing one - that dependency direction is "
                            + "inverted at runtime via Spring's dependency injection");
}
