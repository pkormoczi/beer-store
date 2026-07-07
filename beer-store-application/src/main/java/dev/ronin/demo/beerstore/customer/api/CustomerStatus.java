package dev.ronin.demo.beerstore.customer.api;

/**
 * A customer's lifecycle status. Simple enough (no behavior worth hiding) to live directly in
 * {@code api}, reused by both the DTOs and the internal domain model - much like
 * {@link BeerStyle}/{@code OrderStatus} in their own modules.
 */
public enum CustomerStatus {
    ACTIVE,
    SUSPENDED
}
