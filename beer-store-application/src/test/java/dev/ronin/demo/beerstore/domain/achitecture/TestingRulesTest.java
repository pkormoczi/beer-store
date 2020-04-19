package dev.ronin.demo.beerstore.domain.achitecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.core.importer.Locations;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.LocationProvider;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import dev.ronin.demo.beerstore.BeerStoreApplicationIT;
import dev.ronin.demo.beerstore.base.IntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.type;
import static com.tngtech.archunit.lang.conditions.ArchConditions.accessClassesThat;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "dev.ronin.demo.beerstore", locations = TestingRulesTest.MyTestsLocation.class,
        importOptions = {ImportOption.DoNotIncludeArchives.class, ImportOption.DoNotIncludeJars.class})
@SuppressWarnings("squid:S2187")
public class TestingRulesTest {

    public static final ArchCondition<JavaClass> USE_JUNIT_ASSERTIONS =
            accessClassesThat(type(Assertions.class))
                    .as("use org.junit.jupiter.api.Assertions");

    @ArchTest
    public static final ArchRule springBootTestShouldBeNamedIT =
            classes().that()
                    .areAnnotatedWith(SpringBootTest.class).or()
                    .areAnnotatedWith(DataJpaTest.class).or()
                    .areAnnotatedWith(WebMvcTest.class).and()
                    .doNotHaveModifier(JavaModifier.ABSTRACT)
                    .should().haveSimpleNameEndingWith("IT")
                    .because("we should run them with failsafe plugin!");

    @ArchTest
    public static final ArchRule springBootTestShouldExtendBaseClass =
            classes().that().haveSimpleNameEndingWith("IT").and().areNotAnnotatedWith(DataJpaTest.class)
                    .should().beAssignableTo(IntegrationTest.class)
                    .because("we should optimize IT tests!");

    @ArchTest
    public static final ArchRule testsShouldNotUseJunitAssertions1 =
            noClasses().that().haveSimpleNameEndingWith("Test")
                    .should().dependOnClassesThat().haveFullyQualifiedName("org.junit.jupiter.api.Assertions")
                    .because("we should use org.assertj.core.api.Assertions instead!");

    @ArchTest
    public static final ArchRule testsShouldNotUseJunitAssertions2 =
            noClasses().that().haveSimpleNameEndingWith("Test")
                    .should(USE_JUNIT_ASSERTIONS)
                    .because("we should use org.assertj.core.api.Assertions instead!");


    public static class MyTestsLocation implements LocationProvider {
        @Override
        public Set<Location> get(Class<?> aClass) {
            return Locations.ofClass(BeerStoreApplicationIT.class);
        }
    }
}
