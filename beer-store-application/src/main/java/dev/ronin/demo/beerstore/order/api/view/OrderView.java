package dev.ronin.demo.beerstore.order.api.view;

import java.util.List;

import dev.ronin.demo.beerstore.order.api.type.OrderStatus;

public record OrderView(Long id, OrderStatus orderStatus, Long customerId, List<Long> beers) {
}
