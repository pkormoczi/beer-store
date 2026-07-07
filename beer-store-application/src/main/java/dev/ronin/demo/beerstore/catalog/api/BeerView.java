package dev.ronin.demo.beerstore.catalog.api;

import dev.ronin.demo.beerstore.shared.kernel.Money;

public record BeerView(Long id, String name, BeerStyle beerStyle, Money price) {
}
