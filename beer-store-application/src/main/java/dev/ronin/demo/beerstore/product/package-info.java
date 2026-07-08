/**
 * The product module (the beer domain). Exposes its command/query/view DTOs (in the
 * {@code api.command}/{@code api.query}/{@code api.view} subpackages) and
 * {@link dev.ronin.demo.beerstore.product.api.BeerManagement} from the {@code api} package (a
 * Spring Modulith named interface, contributed to by every {@code api.*} subpackage); everything
 * under {@code internal} is an implementation detail, including the {@code Beer} aggregate
 * itself. Depends on nothing else besides the always-open {@code shared} module - unlike
 * {@code customer}/{@code order}, product has no inbound REST adapter yet, so it needs neither
 * {@code platform :: rest} nor {@code platform :: security}.
 */
@ApplicationModule(allowedDependencies = {"shared"})
package dev.ronin.demo.beerstore.product;

import org.springframework.modulith.ApplicationModule;
