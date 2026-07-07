/**
 * The catalog module (the beer domain). Exposes its Command/Query/View DTOs and
 * {@link dev.ronin.demo.beerstore.catalog.api.ManageBeersUseCase} from the {@code api}
 * subpackage (a Spring Modulith named interface); everything under {@code internal} is an
 * implementation detail, including the {@code Beer} aggregate itself.
 * Depends on nothing else besides the always-open {@code shared}/{@code infrastructure} modules.
 */
@ApplicationModule(allowedDependencies = {"shared", "infrastructure"})
package dev.ronin.demo.beerstore.catalog;

import org.springframework.modulith.ApplicationModule;
