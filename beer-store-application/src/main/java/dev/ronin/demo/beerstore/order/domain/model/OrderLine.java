package dev.ronin.demo.beerstore.order.domain.model;

import dev.ronin.demo.beerstore.shared.kernel.Money;

import java.util.Objects;

/**
 * A single line of an order: a beer id plus a name/price snapshot taken at the moment the order
 * was placed, and the quantity ordered. The snapshot is deliberate - if the beer's name or price
 * changes later, an already-placed order must not change retroactively, since it's a historical
 * document (see {@code Order}). Never holds a live {@code product.api.view.BeerView} reference.
 */
public record OrderLine(Long beerId, String beerNameSnapshot, Money unitPriceSnapshot, int quantity) {

    public OrderLine {
        Objects.requireNonNull(beerId, "beerId must not be null");
        requireNonBlank(beerNameSnapshot, "beerNameSnapshot");
        Objects.requireNonNull(unitPriceSnapshot, "unitPriceSnapshot must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }

    public Money lineTotal() {
        return unitPriceSnapshot.multiply(quantity);
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
