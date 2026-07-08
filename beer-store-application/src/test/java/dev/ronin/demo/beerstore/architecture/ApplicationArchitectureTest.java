package dev.ronin.demo.beerstore.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Module-boundary enforcement itself lives in {@link ModularityTests} (Spring Modulith's
 * {@code ApplicationModules.verify()}), which replaces the old global "Domain/Adapter/
 * Infrastructure" layered-architecture rule this class used to hold. What remains here are the
 * naming/dependency conventions that apply across every module's internal layering.
 */
@AnalyzeClasses(packages = "dev.ronin.demo.beerstore",
        importOptions = {
                ImportOption.DoNotIncludeTests.class,
                ImportOption.DoNotIncludeArchives.class,
                ImportOption.DoNotIncludeJars.class})
@SuppressWarnings("squid:S2187")
public class ApplicationArchitectureTest {

    private static final DescribedPredicate<JavaClass> CONCRETE_AGGREGATE_SERVICE_WITH_MANAGEMENT_PORT =
            DescribedPredicate.describe("a concrete @Service aggregate that implements a *Management port",
                    javaClass -> !javaClass.isInterface()
                            && javaClass.isAnnotatedWith(Service.class)
                            && javaClass.getAllRawInterfaces().stream()
                                    .anyMatch(i -> i.getSimpleName().endsWith("Management")));

    @ArchTest
    public static final ArchRule webAdaptersShouldDependOnManagementPortsNotConcreteAggregateServices =
            noClasses().that().resideInAPackage("..web..")
                    .should().dependOnClassesThat(CONCRETE_AGGREGATE_SERVICE_WITH_MANAGEMENT_PORT)
                    .because("the driving side of each module should depend on the *Management port, "
                            + "mirroring how the persistence adapters depend only on the *Repository port");

}
