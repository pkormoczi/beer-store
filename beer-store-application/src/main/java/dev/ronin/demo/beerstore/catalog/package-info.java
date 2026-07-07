/**
 * The catalog module (the beer domain). Exposes its Command/Query/View DTOs and
 * {@link dev.ronin.demo.beerstore.catalog.api.ManageBeersUseCase} from the {@code api}
 * subpackage (a Spring Modulith named interface); everything under {@code internal} is an
 * implementation detail, including the {@code Beer} aggregate itself.
 * Depends on nothing else besides the always-open {@code shared} module - unlike
 * {@code customer}/{@code order}, catalog has no inbound REST adapter yet, so it needs neither
 * {@code platform :: rest} nor {@code platform :: security}.
 */
@ApplicationModule(allowedDependencies = {"shared"})
package dev.ronin.demo.beerstore.catalog;

import org.springframework.modulith.ApplicationModule;
