package dev.ronin.demo.beerstore.order.api;

import java.util.List;

public class UnknownBeerException extends RuntimeException {

    public UnknownBeerException(List<Long> requestedBeerIds) {
        super("One or more beers not found: " + requestedBeerIds);
    }
}
