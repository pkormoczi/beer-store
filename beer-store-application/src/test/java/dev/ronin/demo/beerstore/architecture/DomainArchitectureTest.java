package dev.ronin.demo.beerstore.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static dev.ronin.demo.beerstore.architecture.ArchitectureSupport.APPLICATION_PACKAGE;
import static dev.ronin.demo.beerstore.architecture.ArchitectureSupport.DOMAIN_PACKAGE;
import static dev.ronin.demo.beerstore.architecture.ArchitectureSupport.MANAGEMENT_POSTFIX;

@AnalyzeClasses(packages = "dev.ronin.demo.beerstore",
        importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeArchives.class, ImportOption.DoNotIncludeJars.class})
@SuppressWarnings("squid:S2187")
class DomainArchitectureTest {

    private static final String JPA_ENTITY_POSTFIX = "JpaEntity";
    private static final String REPOSITORY_POSTFIX = "Repository";
    private static final String JPA_REPOSITORY_POSTFIX = "JpaRepository";
    private static final String ENTITY_PACKAGE = "..persistence.jpa.entity..";

    @ArchTest
    public static final ArchRule repositoryPortsShouldBeInApplicationPackage =
            classes().that().areInterfaces().and().areAnnotatedWith(Repository.class)
                    .should().resideInAPackage(APPLICATION_PACKAGE);

    @ArchTest
    public static final ArchRule entitiesShouldBeInPersistencePackage =
            classes().that().areAnnotatedWith(Entity.class)
                    .should().resideInAPackage(ENTITY_PACKAGE)
                    .because("*JpaEntity classes live one level deeper than the persistence adapter "
                            + "itself, in the dedicated jpa.entity subpackage");

    @ArchTest
    public static final ArchRule entitiesShouldBeNamedJpaEntity =
            classes().that().areAnnotatedWith(Entity.class)
                    .should().haveSimpleNameEndingWith(JPA_ENTITY_POSTFIX)
                    .because("the *JpaEntity suffix makes explicit that this is a JPA-technology-specific "
                            + "persistence model, distinct from the domain.model aggregate");

    private static final DescribedPredicate<JavaClass> ANNOTATED_WITH_SERVICE_OR_NESTED_IN_ONE =
            DescribedPredicate.describe("annotated with @Service, or a nested class of one",
                    javaClass -> javaClass.isAnnotatedWith(Service.class)
                            || javaClass.getEnclosingClass().map(enclosing -> enclosing.isAnnotatedWith(Service.class)).orElse(false));

    @ArchTest
    public static final ArchRule repositoriesShouldHaveOnlyAccessedByServices =
            classes().that().areInterfaces().and().areAnnotatedWith(Repository.class)
                    .should().onlyBeAccessed()
                    .byClassesThat(ANNOTATED_WITH_SERVICE_OR_NESTED_IN_ONE);

    @ArchTest
    public static final ArchRule servicesShouldBePublic =
            classes().that().areAnnotatedWith(Service.class)
                    .should().bePublic();

    @ArchTest
    public static final ArchRule repositoriesShouldBeNamedRepository =
            classes().that().areInterfaces().and().areAnnotatedWith(Repository.class)
                    .should().haveSimpleNameEndingWith(REPOSITORY_POSTFIX);

    @ArchTest
    public static final ArchRule repositoriesShouldBeAnnotatedWithRepository =
            classes().that().haveSimpleNameEndingWith(REPOSITORY_POSTFIX)
                    .and().haveSimpleNameNotEndingWith(JPA_REPOSITORY_POSTFIX)
                    .should().beAnnotatedWith(Repository.class)
                    .because("the Spring Data *JpaRepository interfaces are intentionally not "
                            + "@Repository-annotated themselves - only the application-owned "
                            + "*Repository ports they back are");

    @ArchTest
    public static final ArchRule domainAndApplicationShouldNotDependOnPersistenceTechnology =
            noClasses().that().resideInAPackage("dev.ronin.demo.beerstore.customer.api..")
                    .or().resideInAPackage("dev.ronin.demo.beerstore.order.api..")
                    .or().resideInAPackage("dev.ronin.demo.beerstore.product.api..")
                    .or().resideInAPackage(APPLICATION_PACKAGE)
                    .or().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat().resideInAnyPackage("org.springframework.data..", "jakarta.persistence..")
                    .because("the domain model and application services should only know their own repository "
                            + "ports, not the persistence technology - only the persistence subpackage may depend on JPA/Spring Data");

    private static final DescribedPredicate<JavaClass> INBOUND_PORT_INTERFACE =
            DescribedPredicate.describe("an interface implemented by a @Service aggregate root",
                    javaClass -> javaClass.isInterface()
                            && javaClass.getSubclasses().stream().anyMatch(sub -> sub.isAnnotatedWith(Service.class)));

    @ArchTest
    public static final ArchRule inboundPortsShouldBeNamedManagement =
            classes().that(INBOUND_PORT_INTERFACE)
                    .should().haveSimpleNameEndingWith(MANAGEMENT_POSTFIX)
                    .because("inbound (driving) ports are named after the capability they expose, "
                            + "consistently across modules");

    @ArchTest
    public static final ArchRule managementPortsShouldBeInterfacesResidingInApiPackage =
            classes().that().haveSimpleNameEndingWith(MANAGEMENT_POSTFIX)
                    .should().resideInAnyPackage("dev.ronin.demo.beerstore.customer.api", "dev.ronin.demo.beerstore.order.api",
                            "dev.ronin.demo.beerstore.product.api")
                    .andShould().beInterfaces();
}
