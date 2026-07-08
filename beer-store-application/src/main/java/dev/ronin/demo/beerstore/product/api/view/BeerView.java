package dev.ronin.demo.beerstore.product.api.view;

import dev.ronin.demo.beerstore.product.api.type.BeerStyle;
import dev.ronin.demo.beerstore.shared.kernel.Money;

public record BeerView(Long id, String name, BeerStyle beerStyle, double abv, Money price) {
}
