package dev.ronin.demo.beerstore.domain.achitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "dev.ronin.demo.beerstore.domain",
        importOptions = {ImportOption.DoNotIncludeTests.class,ImportOption.DoNotIncludeArchives.class, ImportOption.DoNotIncludeJars.class})
class DomainArchitectureTest {

    private static final String REPOSITORY = ".*Repository";
    @ArchTest
    public static final ArchRule repositoriesShouldBeNamedRepository =
            classes().that().areAnnotatedWith(Repository.class)
                    .should().haveNameMatching(REPOSITORY);

    @ArchTest
    public static final ArchRule repositoriesShouldBeAnnotatedWithRepository =
            classes().that().haveNameMatching(REPOSITORY)
                    .should().beAnnotatedWith(Repository.class);


    private static final String SERVICE = ".*Service";
    @ArchTest
    public static final ArchRule servicesShouldBeNamedService =
            classes().that().areAnnotatedWith(Service.class)
                    .should().haveNameMatching(SERVICE);


    @ArchTest
    public static final ArchRule servicesShouldBeAnnotatedWithService =
            classes().that().haveNameMatching(SERVICE)
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

    @ArchTest
    public static final ArchRule domainClassesShouldNotAccessInfrastructureClasses =
            noClasses().that().resideInAPackage("..beerstore.domain..")
            .should().accessClassesThat().resideInAPackage("..beerstore.infrastructure..");
}
