package dev.ronin.demo.beerstore.customer.api;

import java.util.List;
import java.util.Optional;

import dev.ronin.demo.beerstore.customer.api.command.ActivateCustomer;
import dev.ronin.demo.beerstore.customer.api.command.DeleteCustomer;
import dev.ronin.demo.beerstore.customer.api.command.RegisterCustomer;
import dev.ronin.demo.beerstore.customer.api.command.SuspendCustomer;
import dev.ronin.demo.beerstore.customer.api.command.UpdateCustomer;
import dev.ronin.demo.beerstore.customer.api.query.FindCustomerByName;
import dev.ronin.demo.beerstore.customer.api.query.GetCustomer;
import dev.ronin.demo.beerstore.customer.api.view.CustomerView;

/**
 * Inbound (driving) port for the customer aggregate, and the customer module's exposed API.
 * Works exclusively with command/query/view DTOs, never with the internal domain model - the
 * translation happens inside
 * {@link dev.ronin.demo.beerstore.customer.internal.application.service.Customers}.
 */
public interface CustomerManagement {

    CustomerView registerCustomer(RegisterCustomer command);

    CustomerView getCustomer(GetCustomer query);

    Optional<CustomerView> findCustomerByName(FindCustomerByName query);

    List<CustomerView> listCustomers();

    CustomerView updateCustomer(UpdateCustomer command);

    void deleteCustomer(DeleteCustomer command);

    CustomerView suspendCustomer(SuspendCustomer command);

    CustomerView activateCustomer(ActivateCustomer command);
}
