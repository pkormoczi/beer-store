package dev.ronin.demo.beerstore.shared.kernel;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A narrow shared-kernel value type: an amount of money in the store's single implied currency
 * (no {@code CurrencyCode} - this is a proportional, single-currency showcase, not a multi-
 * currency system). Immutable and self-validating (never negative), so a {@link Money} is always
 * in a valid state once constructed. Lives in the always-open {@code shared} module - deliberately
 * not in {@code catalog.api} - because both {@code catalog} (a beer's price) and {@code order}
 * (an order line's unit-price snapshot and total) need it, and {@code order} never imports another
 * module's {@code api} types directly (see its anti-corruption {@code BeerLookup} port).
 */
public record Money(BigDecimal amount) {

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }

    public Money add(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money multiply(int quantity) {
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)));
    }
}
