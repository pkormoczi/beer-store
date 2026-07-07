package dev.ronin.demo.beerstore.order;

/**
 * Published after an order is successfully placed. Demonstrates the event-driven side of the
 * hybrid customer/order integration: {@link dev.ronin.demo.beerstore.order.application.Orders}
 * still validates the customer synchronously through {@code ManageCustomersUseCase} before
 * placing the order, but downstream reactions to the placement itself are decoupled via this
 * event and the JPA event publication registry (see {@code spring-modulith-starter-jpa}).
 */
public record OrderPlaced(Long orderId, Long customerId) {
}
