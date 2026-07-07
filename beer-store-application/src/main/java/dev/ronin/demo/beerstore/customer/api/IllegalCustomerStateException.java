package dev.ronin.demo.beerstore.customer.api;

/**
 * Thrown when a lifecycle operation (suspend/activate) is attempted on a {@code Customer} already
 * in that state - mirrors {@code order.api.IllegalOrderStateException}.
 */
public class IllegalCustomerStateException extends RuntimeException {

    public IllegalCustomerStateException(CustomerStatus from, CustomerStatus to) {
        super("Cannot transition customer from %s to %s".formatted(from, to));
    }
}
