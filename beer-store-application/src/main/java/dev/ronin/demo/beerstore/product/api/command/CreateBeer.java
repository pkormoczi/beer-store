package dev.ronin.demo.beerstore.product.api.command;

import dev.ronin.demo.beerstore.product.api.type.BeerStyle;
import dev.ronin.demo.beerstore.shared.kernel.Money;

public record CreateBeer(String name, BeerStyle beerStyle, double abv, Money price) {
}
