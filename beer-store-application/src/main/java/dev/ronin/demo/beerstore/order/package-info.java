/**
 * The order aggregate module. Exposes {@link dev.ronin.demo.beerstore.order.Order},
 * {@link dev.ronin.demo.beerstore.order.Beer}, {@link dev.ronin.demo.beerstore.order.OrderStatus},
 * {@link dev.ronin.demo.beerstore.order.OrderPlaced} and
 * {@link dev.ronin.demo.beerstore.order.ManageOrdersUseCase} as its public API; the
 * {@code application}, {@code persistence} and {@code web} subpackages are internal
 * implementation details. Only allowed to depend on the {@code customer} module (to validate
 * the customer an order is placed for) besides the always-open {@code shared} and
 * {@code infrastructure} modules.
 */
@ApplicationModule(allowedDependencies = {"customer", "shared", "infrastructure"})
package dev.ronin.demo.beerstore.order;

import org.springframework.modulith.ApplicationModule;
