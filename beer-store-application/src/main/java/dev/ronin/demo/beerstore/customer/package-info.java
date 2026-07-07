/**
 * The customer aggregate module. Exposes its Command/Query/View DTOs, the shared
 * {@link dev.ronin.demo.beerstore.customer.api.Address} value type and
 * {@link dev.ronin.demo.beerstore.customer.api.ManageCustomersUseCase} from the {@code api}
 * subpackage (a Spring Modulith named interface); everything under {@code internal} is an
 * implementation detail, including the {@code Customer} aggregate itself.
 */
@ApplicationModule(allowedDependencies = {"shared", "infrastructure"})
package dev.ronin.demo.beerstore.customer;

import org.springframework.modulith.ApplicationModule;
