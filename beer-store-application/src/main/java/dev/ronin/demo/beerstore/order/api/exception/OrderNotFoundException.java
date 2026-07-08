package dev.ronin.demo.beerstore.order.api.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("Order not found: id=" + id);
    }
}
