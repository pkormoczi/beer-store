package dev.ronin.demo.beerstore.product.domain.model;

import dev.ronin.demo.beerstore.product.api.type.BeerAvailability;
import dev.ronin.demo.beerstore.product.api.type.BeerStyle;
import dev.ronin.demo.beerstore.shared.kernel.Money;

import java.util.Objects;

/**
 * The beer aggregate. Self-validating (a {@code name} must not be blank, {@code beerStyle}/
 * {@code price}/{@code availability} must not be null, {@code abv} must be within 0..100) via the
 * compact constructor, so a {@link Beer} is always in a valid state once constructed - unlike the
 * previous bare-record version that enforced nothing.
 * {@link #create(String, BeerStyle, double, Money, BeerAvailability)} is the single entry point
 * for bringing a new beer into existence (id is DB-assigned, so it's deliberately not a
 * constructor parameter here); {@link #create(String, BeerStyle, double, Money)} is a convenience
 * overload defaulting to {@link BeerAvailability#IN_STOCK} for call sites that don't care.
 */
public record Beer(Long id, String name, BeerStyle beerStyle, double abv, Money price, BeerAvailability availability) {

    public Beer {
        requireNonBlank(name, "name");
        Objects.requireNonNull(beerStyle, "beerStyle must not be null");
        requireValidAbv(abv);
        Objects.requireNonNull(price, "price must not be null");
        Objects.requireNonNull(availability, "availability must not be null");
    }

    public static Beer create(String name, BeerStyle beerStyle, double abv, Money price, BeerAvailability availability) {
        return new Beer(null, name, beerStyle, abv, price, availability);
    }

    public static Beer create(String name, BeerStyle beerStyle, double abv, Money price) {
        return create(name, beerStyle, abv, price, BeerAvailability.IN_STOCK);
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    private static void requireValidAbv(double abv) {
        if (abv < 0 || abv > 100) {
            throw new IllegalArgumentException("abv must be between 0 and 100");
        }
    }
}
