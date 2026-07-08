package dev.ronin.demo.beerstore.product.api.type;

/**
 * The field a catalog browse result can be sorted by - mirrors the contract's
 * {@code BeerSortField} schema, kept as a domain-owned type since {@code api}/{@code application}/
 * {@code domain} must not depend on the generated OpenAPI DTOs.
 */
public enum BeerSortField {
    NAME,
    STYLE,
    ABV,
    PRICE,
    AVAILABILITY
}
