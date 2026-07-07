/**
 * The product module (the beer domain). Exposes its Command/Query/View DTOs and
 * {@link dev.ronin.demo.beerstore.product.api.ManageBeersUseCase} from the {@code api}
 * subpackage (a Spring Modulith named interface); everything under {@code internal} is an
 * implementation detail, including the {@code Beer} aggregate itself.
 * Depends on nothing else besides the always-open {@code shared} module - unlike
 * {@code customer}/{@code order}, product has no inbound REST adapter yet, so it needs neither
 * {@code platform :: rest} nor {@code platform :: security}.
 */
@ApplicationModule(allowedDependencies = {"shared"})
package dev.ronin.demo.beerstore.product;

import org.springframework.modulith.ApplicationModule;
