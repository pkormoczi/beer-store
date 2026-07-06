package dev.ronin.demo.beerstore.domain.achitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "dev.ronin.demo.beerstore.adapter",
        importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeArchives.class, ImportOption.DoNotIncludeJars.class})
@SuppressWarnings("squid:S2187")
class AdapterArchitectureTest {

    private static final String REST_PACKAGE = "..adapter.in.rest..";
    private static final String MAPPER_PACKAGE = "..adapter.in.mapper..";
    private static final String ADAPTER_IN_PACKAGE = "..adapter.in..";
    private static final String PERSISTENCE_PACKAGE = "..adapter.out.persistence..";
    private static final String CONTROLLER_POSTFIX = "Controller";
    private static final String ADAPTER_POSTFIX = "Adapter";
    private static final String PERSISTENCE_ADAPTER_POSTFIX = "PersistenceAdapter";
    private static final String MAPPER_POSTFIX = "Mapper";

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
    public static final ArchRule controllersShouldBeInRestPackage =
            classes().that().haveSimpleNameEndingWith(CONTROLLER_POSTFIX)
                    .should().resideInAPackage(REST_PACKAGE);

    @ArchTest
    public static final ArchRule adaptersShouldBeNamedAdapter =
            classes().that().areAnnotatedWith(Service.class)
                    .should().haveSimpleNameEndingWith(ADAPTER_POSTFIX);

    @ArchTest
    public static final ArchRule adaptersShouldBeAnnotatedWithService =
            classes().that().haveSimpleNameEndingWith(ADAPTER_POSTFIX)
                    .and().haveSimpleNameNotEndingWith(PERSISTENCE_ADAPTER_POSTFIX)
                    .should().beAnnotatedWith(Service.class);

    @ArchTest
    public static final ArchRule adaptersShouldBeInAdapterInPackage =
            classes().that().haveSimpleNameEndingWith(ADAPTER_POSTFIX)
                    .and().haveSimpleNameNotEndingWith(PERSISTENCE_ADAPTER_POSTFIX)
                    .should().resideInAPackage(ADAPTER_IN_PACKAGE);

    @ArchTest
    public static final ArchRule mappersShouldBeInMapperPackage =
            classes().that().haveSimpleNameEndingWith(MAPPER_POSTFIX)
                    .should().resideInAPackage(MAPPER_PACKAGE);

    @ArchTest
    public static final ArchRule persistenceAdaptersShouldBeAnnotatedWithRepository =
            classes().that().haveSimpleNameEndingWith(PERSISTENCE_ADAPTER_POSTFIX)
                    .should().beAnnotatedWith(Repository.class);

    @ArchTest
    public static final ArchRule persistenceAdaptersShouldBeInPersistencePackage =
            classes().that().haveSimpleNameEndingWith(PERSISTENCE_ADAPTER_POSTFIX)
                    .should().resideInAPackage(PERSISTENCE_PACKAGE);

}
