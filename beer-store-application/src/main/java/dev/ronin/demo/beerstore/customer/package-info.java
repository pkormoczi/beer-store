/**
 * The customer aggregate module. Exposes {@link dev.ronin.demo.beerstore.customer.Customer},
 * {@link dev.ronin.demo.beerstore.customer.Address} and
 * {@link dev.ronin.demo.beerstore.customer.ManageCustomersUseCase} as its public API; the
 * {@code application}, {@code persistence}, {@code web} and {@code soap} subpackages are
 * internal implementation details.
 */
@ApplicationModule(allowedDependencies = {"shared", "infrastructure"})
package dev.ronin.demo.beerstore.customer;

import org.springframework.modulith.ApplicationModule;
