package dev.ronin.demo.beerstore.domain.achitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction;
import org.junit.Before;
import org.junit.Test;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class ArchitectureTest {

    private JavaClasses classes;

    @Before
    public void setUp() {
        classes = new ClassFileImporter().importPackages("dev.ronin.demo.beerstore.domain");
    }

    @Test
    public void repositoryAnnotationAndNamingConvention() {
        ClassesShouldConjunction annotationToNaming =
                classes().that().areAnnotatedWith(Repository.class)
                         .should().haveNameMatching(".*Repository");
        annotationToNaming.check(this.classes);

        ClassesShouldConjunction namingToAnnotation =
                classes().that().haveNameMatching(".*Repository")
                         .should().beAnnotatedWith(Repository.class);

        namingToAnnotation.check(this.classes);
    }

    @Test
    public void serviceAnnotationAndNamingConvention() {
        ClassesShouldConjunction annotationToNaming =
                classes().that().areAnnotatedWith(Service.class)
                         .should().haveNameMatching(".*Service");

        annotationToNaming.check(this.classes);

        ClassesShouldConjunction namingToAnnotation =
                classes().that().haveNameMatching(".*Service")
                         .should().beAnnotatedWith(Service.class);

        namingToAnnotation.check(this.classes);


    }

    @Test
    public void repositoriesAreOnlyAccessedByServices() {

        ClassesShouldConjunction repositoryAccessRule =
                classes().that().areAnnotatedWith(Repository.class)
                         .should().onlyBeAccessed()
                         .byClassesThat().areAnnotatedWith(Service.class);

        repositoryAccessRule.check(this.classes);

    }

    @Test
    public void serviceVisibilityRule() {

        ClassesShouldConjunction publicVisibilityForServices =
                classes().that().areAnnotatedWith(Service.class)
                         .should().bePublic();

        publicVisibilityForServices.check(this.classes);
    }

    @Test
    public void repositoryVisibilityRule() {

        ClassesShouldConjunction packagePrivateVisibilityForRepositories = classes()
                .that().areAnnotatedWith(Repository.class)
                .should().bePackagePrivate();

        packagePrivateVisibilityForRepositories.check(this.classes);
    }
}
