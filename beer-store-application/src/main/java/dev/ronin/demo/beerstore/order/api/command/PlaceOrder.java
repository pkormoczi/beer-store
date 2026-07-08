package dev.ronin.demo.beerstore.order.api.command;

import java.util.List;

public record PlaceOrder(Long customerId, List<Long> beerIds) {
}
