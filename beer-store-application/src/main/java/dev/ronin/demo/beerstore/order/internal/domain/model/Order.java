package dev.ronin.demo.beerstore.order.internal.domain.model;

import dev.ronin.demo.beerstore.order.api.exception.IllegalOrderStateException;
import dev.ronin.demo.beerstore.order.api.type.OrderStatus;
import dev.ronin.demo.beerstore.shared.kernel.Money;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The order aggregate. Self-validating (a customer id and at least one line are always required)
 * via the compact constructor. {@link #place(Long, List)} is the entry point for placing a new
 * order (status always starts {@code NEW}); {@link #transitionTo}/{@link #cancel()} enforce the
 * legal-transition matrix already encoded in {@link OrderStatus#canTransitionTo}, replacing the
 * previous "check in the service, then rebuild the record" pattern.
 */
public record Order(Long id, OrderStatus orderStatus, Long customerId, List<OrderLine> lines) {

    public Order {
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(orderStatus, "orderStatus must not be null");
        lines = List.copyOf(lines);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("An order requires at least one beer");
        }
    }

    public static Order place(Long customerId, List<OrderLine> lines) {
        return new Order(null, OrderStatus.NEW, customerId, lines);
    }

    public Order transitionTo(OrderStatus target) {
        if (!orderStatus.canTransitionTo(target)) {
            throw new IllegalOrderStateException(orderStatus, target);
        }
        return new Order(id, target, customerId, lines);
    }

    public Order cancel() {
        return transitionTo(OrderStatus.CANCELLED);
    }

    public Money totalAmount() {
        return lines.stream().map(OrderLine::lineTotal).reduce(Money::add).orElseThrow();
    }

    /**
     * Expands the order lines back into a flat, repeated-id list (each line's {@code beerId}
     * repeated {@code quantity} times) - preserves the external {@code OrderView.beers}/wire
     * shape exactly as before, so the REST/SOAP contract never had to change for this module to
     * gain real per-line quantity and price/name snapshots internally.
     */
    public List<Long> beerIds() {
        return lines.stream()
                .flatMap(line -> Stream.generate(line::beerId).limit(line.quantity()))
                .toList();
    }
}
