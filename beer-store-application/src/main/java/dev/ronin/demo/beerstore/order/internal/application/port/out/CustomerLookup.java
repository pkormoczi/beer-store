package dev.ronin.demo.beerstore.order.internal.application.port.out;

/**
 * Order's own outbound port for validating a customer exists before placing an order. Shaped
 * around what {@code Orders} actually needs (just an existence check - the caller already knows
 * the {@code customerId}) rather than depending on the customer module's
 * {@code CustomerManagement} directly, so a change to that API only ripples into
 * {@code CustomerLookupAdapter}, not into {@code Orders}.
 */
public interface CustomerLookup {

    void assertCustomerExists(Long customerId);
}
