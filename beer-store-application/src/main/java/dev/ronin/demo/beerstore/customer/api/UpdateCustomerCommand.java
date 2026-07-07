package dev.ronin.demo.beerstore.customer.api;

public record UpdateCustomerCommand(Long id, String firstName, String lastName, Address address) {
}
