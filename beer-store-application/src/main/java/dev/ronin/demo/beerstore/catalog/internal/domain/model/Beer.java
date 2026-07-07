package dev.ronin.demo.beerstore.catalog.internal.domain.model;

import dev.ronin.demo.beerstore.catalog.api.BeerStyle;

public record Beer(Long id, String name, BeerStyle beerStyle) {
}
