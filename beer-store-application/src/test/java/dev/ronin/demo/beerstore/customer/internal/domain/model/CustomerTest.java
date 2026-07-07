package dev.ronin.demo.beerstore.customer.internal.domain.model;

import dev.ronin.demo.beerstore.customer.api.Address;
import dev.ronin.demo.beerstore.customer.api.CustomerStatus;
import dev.ronin.demo.beerstore.customer.api.IllegalCustomerStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;

class CustomerTest {

    private static final Address ADDRESS = new Address("Hungary", "1095", "Budapest", "Teszt utca 1");

    @Test
    @DisplayName("A newly registered customer has no id and is active")
    void registerBuildsActiveUnsavedCustomer() {
        Customer customer = Customer.register("First", "Last", ADDRESS);

        then(customer.id()).isNull();
        then(customer.status()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    @DisplayName("A blank first name is rejected")
    void blankFirstNameIsRejected() {
        assertThatThrownBy(() -> Customer.register(" ", "Last", ADDRESS))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A blank last name is rejected")
    void blankLastNameIsRejected() {
        assertThatThrownBy(() -> Customer.register("First", " ", ADDRESS))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A null address is rejected")
    void nullAddressIsRejected() {
        assertThatThrownBy(() -> Customer.register("First", "Last", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("updateProfile keeps the identity and status, replaces the profile fields")
    void updateProfileKeepsIdentityAndStatus() {
        Customer registered = new Customer(1L, "First", "Last", ADDRESS, CustomerStatus.SUSPENDED);

        Customer updated = registered.updateProfile("Updated", "Name", ADDRESS);

        then(updated.id()).isEqualTo(1L);
        then(updated.firstName()).isEqualTo("Updated");
        then(updated.lastName()).isEqualTo("Name");
        then(updated.status()).isEqualTo(CustomerStatus.SUSPENDED);
    }

    @Test
    @DisplayName("An active customer can be suspended")
    void activeCustomerCanBeSuspended() {
        Customer active = Customer.register("First", "Last", ADDRESS);

        Customer suspended = active.suspend();

        then(suspended.status()).isEqualTo(CustomerStatus.SUSPENDED);
    }

    @Test
    @DisplayName("A suspended customer cannot be suspended again")
    void suspendingAlreadySuspendedCustomerIsRejected() {
        Customer suspended = Customer.register("First", "Last", ADDRESS).suspend();

        assertThatThrownBy(suspended::suspend).isInstanceOf(IllegalCustomerStateException.class);
    }

    @Test
    @DisplayName("A suspended customer can be activated")
    void suspendedCustomerCanBeActivated() {
        Customer suspended = Customer.register("First", "Last", ADDRESS).suspend();

        Customer activated = suspended.activate();

        then(activated.status()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    @DisplayName("An active customer cannot be activated again")
    void activatingAlreadyActiveCustomerIsRejected() {
        Customer active = Customer.register("First", "Last", ADDRESS);

        assertThatThrownBy(active::activate).isInstanceOf(IllegalCustomerStateException.class);
    }
}
