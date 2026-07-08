package dev.ronin.demo.beerstore.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static dev.ronin.demo.beerstore.architecture.ArchitectureSupport.AGGREGATE_ROOT_SERVICE;

/**
 * Module-boundary enforcement itself lives in {@link ModularityTests} (Spring Modulith's
 * {@code ApplicationModules.verify()}), which replaces the old global "Domain/Adapter/
 * Infrastructure" layered-architecture rule this class used to hold. Onion-style intra-module
 * layering lives in {@link LayeredArchitectureTest}. What remains here are the naming/dependency
 * conventions that apply across every module's internal layering.
 */
@AnalyzeClasses(packages = "dev.ronin.demo.beerstore",
        importOptions = {
                ImportOption.DoNotIncludeTests.class,
                ImportOption.DoNotIncludeArchives.class,
                ImportOption.DoNotIncludeJars.class})
@SuppressWarnings("squid:S2187")
public class ApplicationArchitectureTest {

    @ArchTest
    public static final ArchRule inboundAdaptersShouldDependOnManagementPortsNotConcreteAggregateServices =
            noClasses().that().resideInAPackage("..adapter.in..")
                    .should().dependOnClassesThat(AGGREGATE_ROOT_SERVICE)
                    .because("the driving side of each module should depend on the *Management port, "
                            + "mirroring how the persistence adapters depend only on the *Repository port");

    @ArchTest
    public static final ArchRule aggregateServicesShouldResideInApplicationServicePackage =
            classes().that(AGGREGATE_ROOT_SERVICE)
                    .should().resideInAPackage("..application.service..")
                    .because("the concrete *Management implementation (e.g. Customers/Beers/Orders) is "
                            + "the aggregate's own orchestrator, living alongside the module's other "
                            + "application-layer services");

}
