package dev.ronin.demo.beerstore.product.api.exception;

public class BeerNotFoundException extends RuntimeException {

    public BeerNotFoundException(Long id) {
        super("Beer not found: id=" + id);
    }
}
