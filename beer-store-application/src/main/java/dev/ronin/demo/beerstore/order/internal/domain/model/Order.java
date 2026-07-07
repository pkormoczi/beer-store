package dev.ronin.demo.beerstore.order.internal.domain.model;

import dev.ronin.demo.beerstore.order.api.OrderStatus;

import java.util.List;

public record Order(Long id, OrderStatus orderStatus, Long customerId, List<Long> beers) {
}
