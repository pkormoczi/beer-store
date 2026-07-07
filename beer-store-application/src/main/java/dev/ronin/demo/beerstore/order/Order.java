package dev.ronin.demo.beerstore.order;

import java.util.List;

public record Order(Long id, OrderStatus orderStatus, Long customerId, List<Long> beers) {
}
