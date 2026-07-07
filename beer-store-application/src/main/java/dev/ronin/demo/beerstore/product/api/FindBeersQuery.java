package dev.ronin.demo.beerstore.product.api;

import java.util.List;

public record FindBeersQuery(List<Long> ids) {
}
