package dev.ronin.demo.beerstore.domain.achitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "dev.ronin.demo.beerstore.infrastructure",
        importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeArchives.class, ImportOption.DoNotIncludeJars.class})
public class InfrastructureArchitectureTest {

    public static final String CONFIGURATION = ".*Configuration";
    @ArchTest
    public static final ArchRule configurationsShoulBeNamedConfiguration =
            classes().that().areAnnotatedWith(Configuration.class)
                    .should().haveNameMatching(CONFIGURATION);

    @ArchTest
    public static final ArchRule configurationsShoulBeAnnotatedWithConfiguration =
            classes().that().haveNameMatching(CONFIGURATION)
                    .should().beAnnotatedWith(Configuration.class);

    public static final String CONTROLLER = ".*Controller";
    @ArchTest
    public static final ArchRule controllersShouldBeNamedController =
            classes().that().areAnnotatedWith(Controller.class)
                    .or().areAnnotatedWith(RestController.class)
                    .should().haveNameMatching(CONTROLLER);

    @ArchTest
    public static final ArchRule controllersShouldBeAnnotatedWithController =
            classes().that().haveNameMatching(CONTROLLER)
                    .should().beAnnotatedWith(Controller.class)
                    .orShould().beAnnotatedWith(RestController.class);

    public static final String CONTROLLER_PACKAGE = "..controller..";
    @ArchTest
    public static final ArchRule controllersShouldBeInControllerPackage =
            classes().that().haveNameMatching(CONTROLLER)
                    .should().resideInAPackage(CONTROLLER_PACKAGE);

    @ArchTest
    public static final ArchRule loggersShouldBeConstantAndNamedLog =
            ArchRuleDefinition.fields().that().haveRawType(Logger.class)
                    .should().beStatic()
                    .andShould().beFinal()
                    .andShould().haveName("log");
}