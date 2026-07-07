package dev.ronin.demo.beerstore.order.application;

import dev.ronin.demo.beerstore.customer.Customer;

/**
 * Order's own outbound port for validating a customer exists before placing an order.
 * Shaped around what {@code Orders} actually needs, rather than depending on the customer
 * module's {@code ManageCustomersUseCase} directly - so a change to that API only ripples
 * into {@link CustomerLookupAdapter}, not into {@link Orders}.
 */
public interface CustomerLookup {

    Customer getCustomer(Long customerId);
}
