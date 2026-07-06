package dev.ronin.demo.beerstore.domain.achitecture;

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

@AnalyzeClasses(packages = "dev.ronin.demo.beerstore.domain",
        importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeArchives.class, ImportOption.DoNotIncludeJars.class})
@SuppressWarnings("squid:S2187")
class DomainArchitectureTest {

    public static final String ENTITY_POSTFIX = "Entity";
    private static final String REPOSITORY_POSTFIX = "Repository";
    public static final String REPOSITORY_PACKAGE = "..repository..";
    public static final String DATA_PACKAGE = "..data..";

    @ArchTest
    public static final ArchRule repositoriesShouldBeInPackageNamedRepository =
            classes().that().areAnnotatedWith(Repository.class)
                    .should().resideInAPackage(REPOSITORY_PACKAGE);

    @ArchTest
    public static final ArchRule entitiesShouldBeInPackageNamedModel =
            classes().that().areAnnotatedWith(Entity.class)
                    .should().resideInAPackage(DATA_PACKAGE);

    @ArchTest
    public static final ArchRule entitiesShouldNotBeNamedEntity =
            classes().that().areAnnotatedWith(Entity.class)
                    .should().haveSimpleNameNotEndingWith(ENTITY_POSTFIX)
                    .because("JPA Entities are only stupid data bags!");

    @ArchTest
    public static final ArchRule entitiesShouldBeNamedData =
            classes().that().areAnnotatedWith(Entity.class)
                    .should().haveSimpleNameEndingWith("Data")
                    .because("JPA Entities are only stupid data bags!");

    private static final DescribedPredicate<JavaClass> ANNOTATED_WITH_SERVICE_OR_NESTED_IN_ONE =
            DescribedPredicate.describe("annotated with @Service, or a nested class of one",
                    javaClass -> javaClass.isAnnotatedWith(Service.class)
                            || javaClass.getEnclosingClass().map(enclosing -> enclosing.isAnnotatedWith(Service.class)).orElse(false));

    @ArchTest
    public static final ArchRule repositoriesShouldHaveOnlyAccessedByServices =
            classes().that().areAnnotatedWith(Repository.class)
                    .should().onlyBeAccessed()
                    .byClassesThat(ANNOTATED_WITH_SERVICE_OR_NESTED_IN_ONE);

    @ArchTest
    public static final ArchRule servicesShouldBePublic =
            classes().that().areAnnotatedWith(Service.class)
                    .should().bePublic();

    @ArchTest
    public static final ArchRule repositoriesShouldBeNamedRepository =
            classes().that().areAnnotatedWith(Repository.class)
                    .should().haveSimpleNameEndingWith(REPOSITORY_POSTFIX);

    @ArchTest
    public static final ArchRule repositoriesShouldBeAnnotatedWithRepository =
            classes().that().haveNameMatching(REPOSITORY_POSTFIX)
                    .should().beAnnotatedWith(Repository.class);

    @ArchTest
    public static final ArchRule domainShouldNotDependOnSpringData =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("org.springframework.data..")
                    .because("the domain should only know its own repository ports, not the persistence technology");
}
