package dev.ronin.demo.beerstore.order.api.command;

import dev.ronin.demo.beerstore.order.api.type.OrderStatus;

public record UpdateOrderStatus(Long id, OrderStatus newStatus) {
}
