package dev.ronin.demo.beerstore.product.api.type;

/**
 * The direction a chosen {@link BeerSortField} is ordered in - mirrors the contract's
 * {@code SortDirection} schema, kept as a domain-owned type for the same reason as
 * {@link BeerSortField}.
 */
public enum SortDirection {
    ASC,
    DESC
}
