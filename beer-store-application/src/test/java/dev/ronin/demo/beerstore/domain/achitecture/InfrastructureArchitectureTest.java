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
@SuppressWarnings("squid:S2187")
class InfrastructureArchitectureTest {

    private static final String CONTROLLER_PACKAGE = "..controller..";
    private static final String CONFIGURATION_POSTFIX = "Config";
    private static final String CONTROLLER_POSTFIX = "Controller";

    @ArchTest
    public static final ArchRule configurationsShouldBeNamedConfiguration =
            classes().that().areAnnotatedWith(Configuration.class)
                    .should().haveSimpleNameEndingWith(CONFIGURATION_POSTFIX);

    @ArchTest
    public static final ArchRule configurationsShouldBeAnnotatedWithConfiguration =
            classes().that().haveSimpleNameEndingWith(CONFIGURATION_POSTFIX)
                    .should().beAnnotatedWith(Configuration.class);

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
    public static final ArchRule controllersShouldBeInControllerPackage =
            classes().that().haveSimpleNameEndingWith(CONTROLLER_POSTFIX)
                    .should().resideInAPackage(CONTROLLER_PACKAGE);

}