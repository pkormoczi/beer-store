package dev.ronin.demo.beerstore.customer;

/**
 * Immutable value object. Every field is required and non-blank, enforced by the compact
 * constructor, so an {@link Address} is always in a valid state once constructed.
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
