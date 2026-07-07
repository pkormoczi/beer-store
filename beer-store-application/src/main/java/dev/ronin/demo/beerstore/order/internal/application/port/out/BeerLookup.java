package dev.ronin.demo.beerstore.order.internal.application.port.out;

import java.util.List;

/**
 * Order's own outbound port for checking that requested beers exist before placing an order.
 * Deliberately returns only the ids that were found, not {@code catalog.api.BeerView} objects -
 * order has no use for anything else about a beer.
 */
public interface BeerLookup {

    List<Long> findExistingIds(List<Long> ids);
}
