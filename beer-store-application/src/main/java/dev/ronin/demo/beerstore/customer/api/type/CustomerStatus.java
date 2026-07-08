package dev.ronin.demo.beerstore.customer.api.type;

/**
 * A customer's lifecycle status. Simple enough (no behavior worth hiding) to live directly in
 * {@code api.type}, reused by both the DTOs and the internal domain model - much like
 * {@code product.api.type.BeerStyle}/{@code order.api.type.OrderStatus} in their own modules.
 */
public enum CustomerStatus {
    ACTIVE,
    SUSPENDED
}
