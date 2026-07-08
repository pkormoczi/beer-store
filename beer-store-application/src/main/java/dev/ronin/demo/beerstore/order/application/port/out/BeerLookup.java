package dev.ronin.demo.beerstore.order.application.port.out;

import java.util.List;

/**
 * Order's own outbound port for checking that requested beers exist before placing an order, and
 * for fetching the name/price snapshot needed to build each {@code OrderLine}. Deliberately
 * returns {@link BeerSnapshot} (order's own ACL type), not {@code product.api.view.BeerView} -
 * order has no use for anything else about a beer.
 */
public interface BeerLookup {

    List<BeerSnapshot> findExisting(List<Long> beerIds);
}
