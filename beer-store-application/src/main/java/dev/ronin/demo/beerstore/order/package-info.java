/**
 * The order aggregate module. Exposes its Command/Query/View DTOs, {@link
 * dev.ronin.demo.beerstore.order.api.OrderStatus} and
 * {@link dev.ronin.demo.beerstore.order.api.ManageOrdersUseCase} from the {@code api}
 * subpackage (a Spring Modulith named interface); everything under {@code internal} is an
 * implementation detail, including the {@code Order} aggregate and the {@code OrderPlaced}
 * domain event (nothing outside this module ever needs it, unlike a "public application event").
 * Only allowed to depend on the {@code customer}/{@code catalog} modules' {@code api} named
 * interfaces (via its own {@code CustomerLookup}/{@code BeerLookup} outbound ports), the
 * {@code platform} module's {@code rest}/{@code security} named interfaces (from its inbound
 * REST adapter) and the always-open {@code shared} module. A bare module name in
 * {@code allowedDependencies} only covers a target module's default (unnamed) package, not an
 * explicitly declared {@code @NamedInterface} - since customer/catalog/platform put everything
 * exposed behind a named interface, the dependency must be spelled out as
 * {@code "<module> :: <interface>"}.
 */
@ApplicationModule(allowedDependencies = {"customer :: api", "catalog :: api", "shared", "platform :: rest", "platform :: security"})
package dev.ronin.demo.beerstore.order;

import org.springframework.modulith.ApplicationModule;
