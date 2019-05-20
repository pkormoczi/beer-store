package dev.ronin.demo.beerstore.domain.achitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "dev.ronin.demo.beerstore.domain",
        importOptions = {ImportOption.DoNotIncludeTests.class,ImportOption.DoNotIncludeArchives.class, ImportOption.DoNotIncludeJars.class})
class DomainArchitectureTest {

    @ArchTest
    public static final ArchRule repositoriesShoulBeNamedRepository =
            classes().that().areAnnotatedWith(Repository.class)
                    .should().haveNameMatching(".*Repository");

    @ArchTest
    public static final ArchRule repositoriesShoulBeAnnotatedWithRepository =
            classes().that().haveNameMatching(".*Repository")
                    .should().beAnnotatedWith(Repository.class);


    @ArchTest
    public static final ArchRule servicesShoulBeNamedService =
            classes().that().areAnnotatedWith(Service.class)
                    .should().haveNameMatching(".*Service");


    @ArchTest
    public static final ArchRule servicesShoulBeAnnotatedWithService =
            classes().that().haveNameMatching(".*Service")
                    .should().beAnnotatedWith(Service.class);

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
    public static final ArchRule repositoriesShouldBePackagePrivate =
            classes().that().areAnnotatedWith(Repository.class)
                    .should().bePackagePrivate();
}
