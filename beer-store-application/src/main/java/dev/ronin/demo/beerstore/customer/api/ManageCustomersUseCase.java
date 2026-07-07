package dev.ronin.demo.beerstore.customer.api;

import java.util.List;
import java.util.Optional;

/**
 * Inbound (driving) port for the customer aggregate, and the customer module's exposed API.
 * Works exclusively with Command/Query/View DTOs, never with the internal domain model - the
 * translation happens inside
 * {@link dev.ronin.demo.beerstore.customer.internal.application.service.Customers}.
 */
public interface ManageCustomersUseCase {

    CustomerView registerCustomer(RegisterCustomerCommand command);

    CustomerView getCustomer(GetCustomerQuery query);

    Optional<CustomerView> findCustomerByName(FindCustomerByNameQuery query);

    List<CustomerView> listCustomers();

    CustomerView updateCustomer(UpdateCustomerCommand command);

    void deleteCustomer(DeleteCustomerCommand command);
}
