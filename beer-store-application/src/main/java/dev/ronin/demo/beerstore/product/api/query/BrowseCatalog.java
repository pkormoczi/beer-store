package dev.ronin.demo.beerstore.product.api.query;

import dev.ronin.demo.beerstore.product.api.type.BeerAvailability;
import dev.ronin.demo.beerstore.product.api.type.BeerSortField;
import dev.ronin.demo.beerstore.product.api.type.BeerStyle;
import dev.ronin.demo.beerstore.product.api.type.SortDirection;

import java.math.BigDecimal;
import java.util.List;

/**
 * The criteria for browsing/searching the catalog - every field is an optional filter except
 * {@code sortBy}/{@code sortDirection}, which the REST adapter always populates (contract
 * defaults: NAME/ASC). Deliberately made up of only primitives, {@link BigDecimal} and this
 * module's own {@code api.type} enums - {@code api}/{@code application}/{@code domain} must not
 * depend on Spring Data or JPA types, so the translation to a {@code Specification}/{@code Sort}
 * happens entirely inside the persistence adapter.
 */
public record BrowseCatalog(String name, BeerStyle beerStyle, Double minAbv, Double maxAbv, BigDecimal minPrice,
        BigDecimal maxPrice, List<BeerAvailability> availabilities, BeerSortField sortBy, SortDirection sortDirection) {
}
