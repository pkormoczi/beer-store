package dev.ronin.demo.beerstore.order.internal.application.port.out;

import dev.ronin.demo.beerstore.shared.kernel.Money;

/**
 * Order's own anti-corruption-layer type for what it needs to know about an existing beer when
 * placing an order (enough to build an {@code OrderLine} snapshot) - deliberately not
 * {@code catalog.api.BeerView}, so a change to that type only ripples into
 * {@code BeerLookupAdapter}, never into {@code Orders} or the domain model.
 */
public record BeerSnapshot(Long beerId, String name, Money price) {
}
