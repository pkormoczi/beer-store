package dev.ronin.demo.beerstore.order.application;

import java.util.List;

/**
 * Order's own outbound port for checking that requested beers exist before placing an order.
 * Deliberately returns only the ids that were found, not {@code catalog.Beer} objects - order
 * has no use for anything else about a beer, so the catalog domain type never needs to be
 * imported outside {@link BeerLookupAdapter}.
 */
public interface BeerLookup {

    List<Long> findExistingIds(List<Long> ids);
}
