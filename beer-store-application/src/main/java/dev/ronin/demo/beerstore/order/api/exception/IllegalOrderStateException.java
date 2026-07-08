package dev.ronin.demo.beerstore.order.api.exception;

import dev.ronin.demo.beerstore.order.api.type.OrderStatus;

public class IllegalOrderStateException extends RuntimeException {

    public IllegalOrderStateException(OrderStatus from, OrderStatus to) {
        super("Cannot transition order from %s to %s".formatted(from, to));
    }
}
