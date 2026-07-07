package dev.ronin.demo.beerstore.catalog.api;

import java.util.List;

/**
 * Inbound (driving) port for the catalog module, and its exposed API. Works exclusively with
 * Command/Query/View DTOs; the translation to/from the internal
 * {@link dev.ronin.demo.beerstore.catalog.internal.domain.model.Beer} happens inside
 * {@link dev.ronin.demo.beerstore.catalog.internal.application.service.Beers}.
 */
public interface ManageBeersUseCase {

    List<BeerView> findAllById(FindBeersQuery query);

    BeerView createBeer(CreateBeerCommand command);
}
