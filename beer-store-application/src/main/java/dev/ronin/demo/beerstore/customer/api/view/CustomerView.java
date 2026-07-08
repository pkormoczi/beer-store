package dev.ronin.demo.beerstore.customer.api.view;

import dev.ronin.demo.beerstore.customer.api.CustomerManagement;
import dev.ronin.demo.beerstore.customer.api.type.Address;
import dev.ronin.demo.beerstore.customer.api.type.CustomerStatus;

/**
 * Read-model projection of a customer, returned by {@link CustomerManagement}. Decoupled
 * from {@code internal.domain.model.Customer} so a refactor of the internal aggregate never
 * ripples into callers of the module's API.
 */
public record CustomerView(Long id, String firstName, String lastName, Address address, CustomerStatus status) {
}
