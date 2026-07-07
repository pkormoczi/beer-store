package dev.ronin.demo.beerstore.order.api;

import java.util.List;

public record OrderView(Long id, OrderStatus orderStatus, Long customerId, List<Long> beers) {
}
