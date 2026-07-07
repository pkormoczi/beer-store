package dev.ronin.demo.beerstore.customer.api;

public record RegisterCustomerCommand(String firstName, String lastName, Address address) {
}
