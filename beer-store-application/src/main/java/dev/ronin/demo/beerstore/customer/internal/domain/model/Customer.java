package dev.ronin.demo.beerstore.customer.internal.domain.model;

import dev.ronin.demo.beerstore.customer.api.Address;

public record Customer(Long id, String firstName, String lastName, Address address) {
}
