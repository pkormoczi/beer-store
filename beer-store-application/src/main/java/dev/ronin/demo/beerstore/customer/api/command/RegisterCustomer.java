package dev.ronin.demo.beerstore.customer.api.command;

import dev.ronin.demo.beerstore.customer.api.type.Address;

public record RegisterCustomer(String firstName, String lastName, Address address) {
}
