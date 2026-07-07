package dev.ronin.demo.beerstore.catalog.api;

import java.util.List;

public record FindBeersQuery(List<Long> ids) {
}
