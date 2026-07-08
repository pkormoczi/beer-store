package dev.ronin.demo.beerstore.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Enforces the packaging convention inside each module's {@code api} package: the root holds only
 * the module's {@code *Management} facade interface(s) plus {@code package-info.java}, and every
 * other kind of type lives in a dedicated, role-named subpackage ({@code command}, {@code query},
 * {@code view}, {@code type}, {@code exception}) - see {@link DomainArchitectureTest} for the
 * {@code *Management} naming/location rules themselves.
 */
@AnalyzeClasses(packages = "dev.ronin.demo.beerstore",
        importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeArchives.class, ImportOption.DoNotIncludeJars.class})
@SuppressWarnings("squid:S2187")
class ApiArchitectureTest {

    /**
     * The three business modules' own {@code api} packages - deliberately NOT a generic
     * {@code "..api.."} pattern, since that would also match the unrelated generated
     * {@code shared.api[.model]} OpenAPI package, which isn't subject to this packaging
     * convention at all.
     */
    private static final String[] MODULE_API_PACKAGES = {
            "dev.ronin.demo.beerstore.customer.api..",
            "dev.ronin.demo.beerstore.order.api..",
            "dev.ronin.demo.beerstore.product.api.."};

    private static final String VIEW_POSTFIX = "View";

    private static final DescribedPredicate<JavaClass> NOT_PACKAGE_INFO =
            DescribedPredicate.describe("not package-info", javaClass -> !javaClass.getSimpleName().equals("package-info"));

    @ArchTest
    public static final ArchRule apiRootShouldContainOnlyInterfaces =
            classes().that().resideInAnyPackage("dev.ronin.demo.beerstore.customer.api", "dev.ronin.demo.beerstore.order.api",
                            "dev.ronin.demo.beerstore.product.api")
                    .and(NOT_PACKAGE_INFO)
                    .should().beInterfaces()
                    .because("the api root is reserved for the module's *Management facade(s) - "
                            + "commands/queries/views/value types/exceptions belong in a role-named subpackage");

    @ArchTest
    public static final ArchRule viewsShouldResideInViewPackage =
            classes().that().resideInAnyPackage(MODULE_API_PACKAGES)
                    .and().haveSimpleNameEndingWith(VIEW_POSTFIX)
                    .should().resideInAnyPackage("..customer.api.view..", "..order.api.view..", "..product.api.view..")
                    .because("read-model DTOs are grouped under api.view across every module");

    @ArchTest
    public static final ArchRule apiExceptionsShouldResideInExceptionPackage =
            classes().that().resideInAnyPackage(MODULE_API_PACKAGES)
                    .and().areAssignableTo(RuntimeException.class)
                    .should().resideInAnyPackage("..customer.api.exception..", "..order.api.exception..", "..product.api.exception..")
                    .because("domain exceptions exposed on a module's api are grouped under api.exception across every module");

    @ArchTest
    public static final ArchRule valueEnumsShouldResideInTypePackage =
            classes().that().resideInAnyPackage(MODULE_API_PACKAGES)
                    .and().areEnums()
                    .should().resideInAnyPackage("..customer.api.type..", "..order.api.type..", "..product.api.type..")
                    .because("simple value types (enums) shared by DTOs and the internal domain model are grouped under api.type");
}
