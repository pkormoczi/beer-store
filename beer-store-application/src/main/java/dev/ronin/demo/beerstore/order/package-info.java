/**
 * The order aggregate module. Exposes {@link dev.ronin.demo.beerstore.order.Order},
 * {@link dev.ronin.demo.beerstore.order.OrderStatus},
 * {@link dev.ronin.demo.beerstore.order.OrderPlaced} and
 * {@link dev.ronin.demo.beerstore.order.ManageOrdersUseCase} as its public API; the
 * {@code application}, {@code persistence} and {@code web} subpackages are internal
 * implementation details. {@code Order.beers} holds plain beer ids, not
 * {@code catalog.Beer} references - orders don't embed another module's domain objects.
 * Only allowed to depend on the {@code customer} and {@code catalog} modules (via its own
 * {@code CustomerLookup}/{@code BeerLookup} outbound ports) besides the always-open
 * {@code shared} and {@code infrastructure} modules.
 */
@ApplicationModule(allowedDependencies = {"customer", "catalog", "shared", "infrastructure"})
package dev.ronin.demo.beerstore.order;

import org.springframework.modulith.ApplicationModule;
