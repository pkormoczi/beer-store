package dev.ronin.demo.beerstore.catalog.api;

import dev.ronin.demo.beerstore.shared.kernel.Money;

public record CreateBeerCommand(String name, BeerStyle beerStyle, Money price) {
}
