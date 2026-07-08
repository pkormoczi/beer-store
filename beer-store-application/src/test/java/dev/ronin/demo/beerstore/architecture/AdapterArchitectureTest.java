package dev.ronin.demo.beerstore.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "dev.ronin.demo.beerstore",
        importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeArchives.class, ImportOption.DoNotIncludeJars.class})
@SuppressWarnings("squid:S2187")
class AdapterArchitectureTest {

    private static final String REST_PACKAGE = "..rest..";
    private static final String PERSISTENCE_PACKAGE = "..persistence..";
    private static final String ADAPTER_PACKAGE = "..adapter..";
    private static final String CONTROLLER_POSTFIX = "Controller";
    private static final String ADAPTER_POSTFIX = "Adapter";
    private static final String PERSISTENCE_ADAPTER_POSTFIX = "PersistenceAdapter";
    /**
     * Cross-module anti-corruption adapters (e.g. order's CustomerLookupAdapter/
     * BeerLookupAdapter), each translating a module-owned outbound *Lookup port to another
     * module's *Management port - a different kind of adapter than the driving *Adapter classes:
     * plain @Component, living in adapter.out.&lt;other module&gt;, not rest/.
     */
    private static final String LOOKUP_ADAPTER_POSTFIX = "LookupAdapter";
    private static final String MAPPER_POSTFIX = "Mapper";

    /**
     * The concrete aggregate-root @Service classes (e.g. Customers/Orders) are a different kind
     * of @Service than the driving *Adapter classes: they implement a *Management port instead of
     * being named *Adapter, so they're excluded from the naming rule below.
     */
    private static final DescribedPredicate<com.tngtech.archunit.core.domain.JavaClass> AGGREGATE_ROOT_SERVICE =
            DescribedPredicate.describe("a concrete @Service implementing a *Management port",
                    javaClass -> !javaClass.isInterface()
                            && javaClass.isAnnotatedWith(Service.class)
                            && javaClass.getAllRawInterfaces().stream().anyMatch(i -> i.getSimpleName().endsWith("Management")));

    @ArchTest
    public static final ArchRule controllersShouldBeNamedController =
            classes().that().areAnnotatedWith(Controller.class)
                    .or().areAnnotatedWith(RestController.class)
                    .should().haveSimpleNameEndingWith(CONTROLLER_POSTFIX);

    @ArchTest
    public static final ArchRule controllersShouldBeAnnotatedWithController =
            classes().that().haveSimpleNameEndingWith(CONTROLLER_POSTFIX)
                    .should().beAnnotatedWith(Controller.class)
                    .orShould().beAnnotatedWith(RestController.class);

    @ArchTest
    public static final ArchRule controllersShouldBeInRestPackage =
            classes().that().haveSimpleNameEndingWith(CONTROLLER_POSTFIX)
                    .should().resideInAPackage(REST_PACKAGE);

    @ArchTest
    public static final ArchRule adaptersShouldBeNamedAdapter =
            classes().that().areAnnotatedWith(Service.class)
                    .and(DescribedPredicate.not(AGGREGATE_ROOT_SERVICE))
                    .should().haveSimpleNameEndingWith(ADAPTER_POSTFIX);

    @ArchTest
    public static final ArchRule adaptersShouldBeAnnotatedWithService =
            classes().that().haveSimpleNameEndingWith(ADAPTER_POSTFIX)
                    .and().haveSimpleNameNotEndingWith(PERSISTENCE_ADAPTER_POSTFIX)
                    .and().haveSimpleNameNotEndingWith(LOOKUP_ADAPTER_POSTFIX)
                    .should().beAnnotatedWith(Service.class);

    @ArchTest
    public static final ArchRule adaptersShouldBeInRestPackage =
            classes().that().haveSimpleNameEndingWith(ADAPTER_POSTFIX)
                    .and().haveSimpleNameNotEndingWith(PERSISTENCE_ADAPTER_POSTFIX)
                    .and().haveSimpleNameNotEndingWith(LOOKUP_ADAPTER_POSTFIX)
                    .should().resideInAPackage(REST_PACKAGE);

    @ArchTest
    public static final ArchRule lookupAdaptersShouldBeAnnotatedWithComponent =
            classes().that().haveSimpleNameEndingWith(LOOKUP_ADAPTER_POSTFIX)
                    .should().beAnnotatedWith(Component.class);

    @ArchTest
    public static final ArchRule lookupAdaptersShouldBeInAdapterPackage =
            classes().that().haveSimpleNameEndingWith(LOOKUP_ADAPTER_POSTFIX)
                    .should().resideInAPackage(ADAPTER_PACKAGE);

    @ArchTest
    public static final ArchRule mappersShouldBeInRestOrPersistencePackage =
            classes().that().haveSimpleNameEndingWith(MAPPER_POSTFIX)
                    .should().resideInAnyPackage(REST_PACKAGE, PERSISTENCE_PACKAGE);

    @ArchTest
    public static final ArchRule persistenceAdaptersShouldBeAnnotatedWithRepository =
            classes().that().haveSimpleNameEndingWith(PERSISTENCE_ADAPTER_POSTFIX)
                    .should().beAnnotatedWith(Repository.class);

    @ArchTest
    public static final ArchRule persistenceAdaptersShouldBeInPersistencePackage =
            classes().that().haveSimpleNameEndingWith(PERSISTENCE_ADAPTER_POSTFIX)
                    .should().resideInAPackage(PERSISTENCE_PACKAGE);

}
