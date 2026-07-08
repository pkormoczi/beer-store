package dev.ronin.demo.beerstore.customer.api.type;

/**
 * Immutable value object. Every field is required and non-blank, enforced by the compact
 * constructor, so an {@link Address} is always in a valid state once constructed. Simple
 * enough (no behavior worth hiding) to live directly in {@code api.type}, shared by the
 * command/view DTOs and the internal domain model alike - much like {@code Money}/
 * {@code CurrencyCode} in a shared kernel.
 */
public record Address(String country, String zip, String city, String streetAddress) {

    public Address {
        requireNonBlank(country, "country");
        requireNonBlank(zip, "zip");
        requireNonBlank(city, "city");
        requireNonBlank(streetAddress, "streetAddress");
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
