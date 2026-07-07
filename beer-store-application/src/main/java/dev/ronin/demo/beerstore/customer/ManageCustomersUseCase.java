package dev.ronin.demo.beerstore.customer;

import java.util.List;
import java.util.Optional;

/**
 * Inbound (driving) port for the customer aggregate, and the customer module's exposed API.
 * Other modules (e.g. {@code order}) and adapters depend on this interface rather than the
 * concrete {@link dev.ronin.demo.beerstore.customer.application.Customers} service.
 */
public interface ManageCustomersUseCase {

    Customer createCustomer(String firstName, String lastName, Address address);

    Customer getCustomer(Long id);

    Optional<Customer> findCustomerByName(String name);

    List<Customer> listCustomers();

    Customer updateCustomer(Long id, String firstName, String lastName, Address address);

    void deleteCustomer(Long id);
}
