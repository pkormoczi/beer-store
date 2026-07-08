package dev.ronin.demo.beerstore.customer.internal.domain.model;

import dev.ronin.demo.beerstore.customer.api.exception.IllegalCustomerStateException;
import dev.ronin.demo.beerstore.customer.api.type.Address;
import dev.ronin.demo.beerstore.customer.api.type.CustomerStatus;

import java.util.Objects;

/**
 * The customer aggregate. Self-validating (non-blank names, non-null address/status) via the
 * compact constructor, so a {@link Customer} is always in a valid state once constructed -
 * unlike the previous bare-record version, which enforced nothing beyond {@link Address}'s own
 * invariants. {@link #register(String, String, Address)} is the entry point for bringing a new
 * customer into existence (status starts {@code ACTIVE}); {@link #updateProfile} and
 * {@link #suspend()}/{@link #activate()} are the intention-revealing operations that replace the
 * previous "load, discard, rebuild from scratch" pattern in the application service.
 */
public record Customer(Long id, String firstName, String lastName, Address address, CustomerStatus status) {

    public Customer {
        requireNonBlank(firstName, "firstName");
        requireNonBlank(lastName, "lastName");
        Objects.requireNonNull(address, "address must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }

    public static Customer register(String firstName, String lastName, Address address) {
        return new Customer(null, firstName, lastName, address, CustomerStatus.ACTIVE);
    }

    public Customer updateProfile(String firstName, String lastName, Address address) {
        return new Customer(id, firstName, lastName, address, status);
    }

    public Customer suspend() {
        if (status == CustomerStatus.SUSPENDED) {
            throw new IllegalCustomerStateException(status, CustomerStatus.SUSPENDED);
        }
        return new Customer(id, firstName, lastName, address, CustomerStatus.SUSPENDED);
    }

    public Customer activate() {
        if (status == CustomerStatus.ACTIVE) {
            throw new IllegalCustomerStateException(status, CustomerStatus.ACTIVE);
        }
        return new Customer(id, firstName, lastName, address, CustomerStatus.ACTIVE);
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
