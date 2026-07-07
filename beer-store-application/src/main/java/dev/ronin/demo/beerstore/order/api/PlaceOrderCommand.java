package dev.ronin.demo.beerstore.order.api;

import java.util.List;

public record PlaceOrderCommand(Long customerId, List<Long> beerIds) {
}
