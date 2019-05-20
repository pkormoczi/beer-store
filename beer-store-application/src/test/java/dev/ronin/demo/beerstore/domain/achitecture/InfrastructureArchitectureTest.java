package dev.ronin.demo.beerstore.domain.achitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "dev.ronin.demo.beerstore.infrastructure",
        importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeArchives.class, ImportOption.DoNotIncludeJars.class})
public class InfrastructureArchitectureTest {

    @ArchTest
    public static final ArchRule configurationsShoulBeNamedConfiguration =
            classes().that().areAnnotatedWith(Configuration.class)
                    .should().haveNameMatching(".*Configuration");

    @ArchTest
    public static final ArchRule configurationsShoulBeAnnotatedWithConfiguration =
            classes().that().haveNameMatching(".*Configuration")
                    .should().beAnnotatedWith(Configuration.class);

    @ArchTest
    public static final ArchRule controllersShouldBeNamedController =
            classes().that().areAnnotatedWith(Controller.class)
                    .or().areAnnotatedWith(RestController.class)
                    .should().haveNameMatching(".*Controller");

    @ArchTest
    public static final ArchRule controllersShouldBeAnnotatedWithController =
            classes().that().haveNameMatching(".*Controller")
                    .should().beAnnotatedWith(Controller.class)
                    .orShould().beAnnotatedWith(RestController.class);

    @ArchTest
    public static final ArchRule controllersShouldBeInControllerPackage =
            classes().that().haveNameMatching(".*Controller")
                    .should().resideInAPackage("..controller..");

}
