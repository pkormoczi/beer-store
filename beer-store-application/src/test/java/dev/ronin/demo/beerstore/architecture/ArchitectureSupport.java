package dev.ronin.demo.beerstore.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import org.springframework.stereotype.Service;

/**
 * Package-name roots and predicates shared by more than one architecture test class, kept here
 * once instead of drifting into slightly different copies (e.g. three near-identical "@Service
 * implementing a *Management port" predicates used to live in {@code DomainArchitectureTest},
 * {@code AdapterArchitectureTest} and {@code ApplicationArchitectureTest}). A constant or
 * predicate used by only one class stays local to that class - this is not a dumping ground for
 * every literal in the suite.
 */
final class ArchitectureSupport {

    static final String MANAGEMENT_POSTFIX = "Management";
    static final String DOMAIN_PACKAGE = "..domain..";
    static final String APPLICATION_PACKAGE = "..application..";

    /**
     * A concrete class annotated {@code @Service} that implements an inbound port named
     * {@code *Management} - the aggregate-root service (e.g. Customers/Beers/Orders), as opposed
     * to a driving {@code *Adapter} (also {@code @Service}, but never implements a
     * {@code *Management} port itself).
     */
    static final DescribedPredicate<JavaClass> AGGREGATE_ROOT_SERVICE =
            DescribedPredicate.describe("a concrete @Service implementing a *" + MANAGEMENT_POSTFIX + " port",
                    javaClass -> !javaClass.isInterface()
                            && javaClass.isAnnotatedWith(Service.class)
                            && javaClass.getAllRawInterfaces().stream()
                                    .anyMatch(i -> i.getSimpleName().endsWith(MANAGEMENT_POSTFIX)));

    private ArchitectureSupport() {
    }
}
