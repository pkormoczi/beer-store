package dev.ronin.demo.beerstore.product.domain.model;

import dev.ronin.demo.beerstore.product.api.type.BeerStyle;
import dev.ronin.demo.beerstore.shared.kernel.Money;

import java.util.Objects;

/**
 * The beer aggregate. Self-validating (a {@code name} must not be blank, {@code beerStyle}/
 * {@code price} must not be null) via the compact constructor, so a {@link Beer} is always in a
 * valid state once constructed - unlike the previous bare-record version that enforced nothing.
 * {@link #create(String, BeerStyle, Money)} is the single entry point for bringing a new beer
 * into existence (id is DB-assigned, so it's deliberately not a constructor parameter here).
 */
public record Beer(Long id, String name, BeerStyle beerStyle, Money price) {

    public Beer {
        requireNonBlank(name, "name");
        Objects.requireNonNull(beerStyle, "beerStyle must not be null");
        Objects.requireNonNull(price, "price must not be null");
    }

    public static Beer create(String name, BeerStyle beerStyle, Money price) {
        return new Beer(null, name, beerStyle, price);
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
