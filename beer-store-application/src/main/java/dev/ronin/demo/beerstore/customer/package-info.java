/**
 * The customer aggregate module. Exposes its Command/Query/View DTOs, the shared
 * {@link dev.ronin.demo.beerstore.customer.api.Address} value type and
 * {@link dev.ronin.demo.beerstore.customer.api.ManageCustomersUseCase} from the {@code api}
 * subpackage (a Spring Modulith named interface); everything under {@code internal} is an
 * implementation detail, including the {@code Customer} aggregate itself. Depends on the
 * {@code platform} module's {@code rest}/{@code security} named interfaces from its inbound
 * REST adapter (for {@code ErrorDetails}/{@code Authorized}) besides the always-open
 * {@code shared} module.
 */
@ApplicationModule(allowedDependencies = {"shared", "platform :: rest", "platform :: security"})
package dev.ronin.demo.beerstore.customer;

import org.springframework.modulith.ApplicationModule;
