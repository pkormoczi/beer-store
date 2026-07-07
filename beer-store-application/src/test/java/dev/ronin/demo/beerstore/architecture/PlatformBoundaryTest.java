package dev.ronin.demo.beerstore.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * {@code platform} is deliberately excluded from {@link ModularityTests}' {@code
 * ApplicationModules} scan - it's a cross-cutting technical foundation, not a business domain, so
 * Spring Modulith should only validate the actual bounded contexts (customer/product/order). That
 * exclusion means the two boundary invariants Modulith's {@code verify()} would otherwise have
 * enforced for {@code platform} have to be pulled in here as plain ArchUnit rules instead: it
 * never depends back on a business module, and its {@code openapi}/{@code ws} subpackages are
 * never reached from outside {@code platform} itself. Scoped to the whole application (not just
 * {@code platform}, unlike {@link PlatformArchitectureTest}) since the second rule needs to see
 * every other package to know nothing outside {@code platform} reaches into it.
 */
@AnalyzeClasses(packages = "dev.ronin.demo.beerstore",
        importOptions = {
                ImportOption.DoNotIncludeTests.class,
                ImportOption.DoNotIncludeArchives.class,
                ImportOption.DoNotIncludeJars.class})
@SuppressWarnings("squid:S2187")
class PlatformBoundaryTest {

    @ArchTest
    static final ArchRule platformShouldNotDependOnBusinessModules =
            noClasses().that().resideInAPackage("..platform..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..customer..", "..order..", "..product..")
                    .because("platform is a technical foundation shared by every business module - "
                            + "it must never depend back on one of them (e.g. a business module's "
                            + "domain exceptions), which is what lets CommonRestExceptionHandler "
                            + "stay generic");

    @ArchTest
    static final ArchRule platformInternalsShouldNotBeAccessedFromOutside =
            noClasses().that().resideOutsideOfPackage("..platform..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..platform.openapi..", "..platform.ws..")
                    .because("openapi/ws are platform's own wiring for the generated REST/SOAP "
                            + "surface - nothing outside platform needs them directly");

}
