/**
 * The product module (the beer domain). Exposes its command/query/view DTOs (in the
 * {@code api.command}/{@code api.query}/{@code api.view} subpackages) and
 * {@link dev.ronin.demo.beerstore.product.api.BeerManagement} from the {@code api} package (a
 * Spring Modulith named interface, contributed to by every {@code api.*} subpackage); every other
 * subpackage ({@code domain}, {@code application}, {@code adapter}) is an implementation detail
 * hidden by Spring Modulith's default rule (module subpackages are internal unless annotated
 * {@code @NamedInterface}), including the {@code Beer} aggregate itself. Its inbound REST adapter
 * (the {@code catalog} browse endpoints) also uses the (Modulith-excluded) {@code platform}
 * package's {@code rest} type ({@code ErrorDetails}) besides the always-open {@code shared}
 * module - the browse endpoints are not yet public, so {@code platform :: security} is not needed
 * (unlike customer/order, which use {@code Authorized} for admin-only operations).
 * {@code Beer} carries a warehouse {@code availability} read-model field (set at creation, never
 * changed by a management operation yet - a future round lets the {@code inventory} module drive
 * it via domain events, see FEAT-INV-1); browsing the catalog supports filtering
 * (name/style/abv/price/availability) and sorting, translated to a JPA {@code Specification}/
 * {@code Sort} entirely inside {@code adapter.out.persistence.jpa} so {@code api}/
 * {@code application}/{@code domain} stay free of persistence-technology types.
 */
@ApplicationModule(allowedDependencies = {"shared"})
package dev.ronin.demo.beerstore.product;

import org.springframework.modulith.ApplicationModule;
