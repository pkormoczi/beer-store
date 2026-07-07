/**
 * The catalog module. Exposes {@link dev.ronin.demo.beerstore.catalog.Beer},
 * {@link dev.ronin.demo.beerstore.catalog.BeerStyle} and
 * {@link dev.ronin.demo.beerstore.catalog.ManageBeersUseCase} as its public API; the
 * {@code application} and {@code persistence} subpackages are internal implementation details.
 * Depends on nothing else besides the always-open {@code shared}/{@code infrastructure} modules.
 */
@ApplicationModule(allowedDependencies = {"shared", "infrastructure"})
package dev.ronin.demo.beerstore.catalog;

import org.springframework.modulith.ApplicationModule;
