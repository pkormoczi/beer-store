package dev.ronin.demo.beerstore.architecture;

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
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.type;
import static com.tngtech.archunit.lang.conditions.ArchConditions.accessClassesThat;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * The one architecture test that deliberately analyzes the test tree itself - note the absence
 * of {@code ImportOption.DoNotIncludeTests} below, unlike every other class in this package. Two
 * self-reference traps this class works around:
 * <ul>
 *   <li>{@code contract}'s own {@code ContractTestBase}/{@code ContractTestBaseMockMvc} are
 *   hand-written, non-abstract {@code @SpringBootTest} base classes for the Spring Cloud
 *   Contract-generated suites ({@code contract.explicit}/{@code contract.mockmvc}) - never run
 *   directly, so exempt from the {@code *IT} naming rule.</li>
 *   <li>every class in this {@code architecture} package (this one included) has a simple name
 *   ending in "Test", and this class itself has to reference {@code Assertions.class} to define
 *   {@link #USE_JUNIT_ASSERTIONS} - so the "no JUnit Assertions" rules exempt the whole {@code
 *   architecture} package, which is meta code, not a test suite for business logic.</li>
 * </ul>
 */
@AnalyzeClasses(packages = "dev.ronin.demo.beerstore", locations = TestingRulesTest.MyTestsLocation.class,
        importOptions = {
                ImportOption.DoNotIncludeArchives.class,
                ImportOption.DoNotIncludeJars.class
        })
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
                    .and().resideOutsideOfPackage("..contract..")
                    .should().haveSimpleNameEndingWith("IT")
                    .because("we should run them with failsafe plugin! (contract's own "
                            + "@SpringBootTest base classes are exempt - see class javadoc; nothing "
                            + "else is directly @SpringBootTest-annotated today, so this rule "
                            + "currently has no matches and is kept alive via allowEmptyShould)")
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule springBootTestShouldExtendBaseClass =
            classes().that().haveSimpleNameEndingWith("IT").and().areNotAnnotatedWith(DataJpaTest.class)
                    .should().beAssignableTo(IntegrationTest.class)
                    .because("we should optimize IT tests!");

    @ArchTest
    public static final ArchRule testsShouldNotUseJunitAssertionsByDependency =
            noClasses().that().haveSimpleNameEndingWith("Test")
                    .and().resideOutsideOfPackage("..architecture..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("org.junit.jupiter.api.Assertions")
                    .because("we should use org.assertj.core.api.Assertions instead! (architecture "
                            + "tests are exempt - see class javadoc)");

    @ArchTest
    public static final ArchRule testsShouldNotUseJunitAssertionsByAccess =
            noClasses().that().haveSimpleNameEndingWith("Test")
                    .and().resideOutsideOfPackage("..architecture..")
                    .should(USE_JUNIT_ASSERTIONS)
                    .because("we should use org.assertj.core.api.Assertions instead! (architecture "
                            + "tests are exempt - see class javadoc)");


    public static class MyTestsLocation implements LocationProvider {
        @Override
        public Set<Location> get(Class<?> aClass) {
            return Locations.ofClass(BeerStoreApplicationIT.class);
        }
    }
}
