package dev.ronin.demo.beerstore.customer.api.command;

import dev.ronin.demo.beerstore.customer.api.type.Address;

public record UpdateCustomer(Long id, String firstName, String lastName, Address address) {
}
