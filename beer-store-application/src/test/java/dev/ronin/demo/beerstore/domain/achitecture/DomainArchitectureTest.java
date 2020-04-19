package dev.ronin.demo.beerstore.domain.achitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.persistence.Entity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

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

    @ArchTest
    public static final ArchRule repositoriesShouldHaveOnlyAccessedByServices =
            classes().that().areAnnotatedWith(Repository.class)
                    .should().onlyBeAccessed()
                    .byClassesThat().areAnnotatedWith(Service.class);

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
}
