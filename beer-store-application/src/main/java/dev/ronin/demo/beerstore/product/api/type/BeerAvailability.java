package dev.ronin.demo.beerstore.product.api.type;

/**
 * The warehouse availability of a beer in the catalog. Set at creation time and, for now, never
 * changed by a dedicated management operation - a future round will let the {@code inventory}
 * module drive it via domain events (see FEAT-INV-1 in the features backbone).
 */
public enum BeerAvailability {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK,
    DISCONTINUED,
    COMING_SOON
}
