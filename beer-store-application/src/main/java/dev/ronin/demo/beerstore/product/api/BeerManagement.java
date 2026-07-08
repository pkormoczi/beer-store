package dev.ronin.demo.beerstore.product.api;

import java.util.List;

import dev.ronin.demo.beerstore.product.api.command.CreateBeer;
import dev.ronin.demo.beerstore.product.api.query.BrowseCatalog;
import dev.ronin.demo.beerstore.product.api.query.FindBeers;
import dev.ronin.demo.beerstore.product.api.query.GetBeer;
import dev.ronin.demo.beerstore.product.api.view.BeerView;

/**
 * Inbound (driving) port for the product module, and its exposed API. Works exclusively with
 * command/query/view DTOs; the translation to/from the internal
 * {@link dev.ronin.demo.beerstore.product.domain.model.Beer} happens inside
 * {@link dev.ronin.demo.beerstore.product.application.service.Beers}.
 */
public interface BeerManagement {

    List<BeerView> findAllById(FindBeers query);

    List<BeerView> browse(BrowseCatalog query);

    BeerView getBeer(GetBeer query);

    BeerView createBeer(CreateBeer command);
}
