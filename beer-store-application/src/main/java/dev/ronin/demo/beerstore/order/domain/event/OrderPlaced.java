package dev.ronin.demo.beerstore.order.domain.event;

/**
 * Published after an order is successfully placed. A genuinely internal domain event (as
 * opposed to a "public application event" living in {@code api}): nothing outside this module
 * ever needs it - {@link dev.ronin.demo.beerstore.order.application.service.Orders}
 * publishes it, and
 * {@link dev.ronin.demo.beerstore.order.application.service.OrderPlacedEventListener}
 * consumes it, both inside {@code order}. Demonstrates the JPA/JDBC event publication registry
 * (see {@code spring-modulith-starter-jdbc}) for the event-driven half of the hybrid
 * customer/order integration - {@code Orders.placeOrder} still validates synchronously via
 * {@code CustomerLookup}/{@code BeerLookup} before placing the order.
 */
public record OrderPlaced(Long orderId, Long customerId) {
}
