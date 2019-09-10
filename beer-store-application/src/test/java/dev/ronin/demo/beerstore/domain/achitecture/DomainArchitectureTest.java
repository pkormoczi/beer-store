package dev.ronin.demo.beerstore.domain.achitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "dev.ronin.demo.beerstore.domain",
        importOptions = {ImportOption.DoNotIncludeTests.class,ImportOption.DoNotIncludeArchives.class, ImportOption.DoNotIncludeJars.class})
class DomainArchitectureTest {

    private static final String REPOSITORY = ".*Repository";
    @ArchTest
    public static final ArchRule repositoriesShoulBeNamedRepository =
            classes().that().areAnnotatedWith(Repository.class)
                    .should().haveNameMatching(REPOSITORY);

    @ArchTest
    public static final ArchRule repositoriesShoulBeAnnotatedWithRepository =
            classes().that().haveNameMatching(REPOSITORY)
                    .should().beAnnotatedWith(Repository.class);


    private static final String SERVICE = ".*Service";
    @ArchTest
    public static final ArchRule servicesShoulBeNamedService =
            classes().that().areAnnotatedWith(Service.class)
                    .should().haveNameMatching(SERVICE);


    @ArchTest
    public static final ArchRule servicesShoulBeAnnotatedWithService =
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
}
