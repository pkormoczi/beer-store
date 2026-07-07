package dev.ronin.demo.beerstore.catalog.api;

public record CreateBeerCommand(String name, BeerStyle beerStyle) {
}
