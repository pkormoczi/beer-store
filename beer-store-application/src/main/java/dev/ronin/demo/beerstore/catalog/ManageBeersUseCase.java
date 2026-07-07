package dev.ronin.demo.beerstore.catalog;

import java.util.List;

/**
 * Inbound (driving) port for the catalog module, and its exposed API. Other modules
 * (e.g. {@code order}, via its own {@code BeerLookup} outbound port and adapter) depend on
 * this interface rather than the concrete
 * {@link dev.ronin.demo.beerstore.catalog.application.Beers} service.
 */
public interface ManageBeersUseCase {

    List<Beer> findAllById(List<Long> ids);

    Beer createBeer(String name, BeerStyle beerStyle);
}
