package dev.ronin.demo.beerstore.domain.achitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.context.annotation.Configuration;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "dev.ronin.demo.beerstore.infrastructure",
        importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeArchives.class, ImportOption.DoNotIncludeJars.class})
@SuppressWarnings("squid:S2187")
class InfrastructureArchitectureTest {

    private static final String CONFIGURATION_POSTFIX = "Config";

    @ArchTest
    public static final ArchRule configurationsShouldBeNamedConfiguration =
            classes().that().areAnnotatedWith(Configuration.class)
                    .should().haveSimpleNameEndingWith(CONFIGURATION_POSTFIX);

    @ArchTest
    public static final ArchRule configurationsShouldBeAnnotatedWithConfiguration =
            classes().that().haveSimpleNameEndingWith(CONFIGURATION_POSTFIX)
                    .should().beAnnotatedWith(Configuration.class);

}