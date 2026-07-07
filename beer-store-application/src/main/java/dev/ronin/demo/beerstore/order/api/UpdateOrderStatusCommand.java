package dev.ronin.demo.beerstore.order.api;

public record UpdateOrderStatusCommand(Long id, OrderStatus newStatus) {
}
