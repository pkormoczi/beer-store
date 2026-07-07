package dev.ronin.demo.beerstore.customer.application;

import dev.ronin.demo.beerstore.base.IntegrationTest;
import dev.ronin.demo.beerstore.customer.Address;
import dev.ronin.demo.beerstore.customer.CustomerNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CustomersIT extends IntegrationTest {

    @Autowired
    Customers customers;

    @Test
    @DisplayName("Customer should be updated")
    void customerShouldBeUpdated() {
        //Given
        Address address = new Address("Hungary", "1095", "Budapest", "Teszt utca 1");
        Long id = customers.createCustomer("TestFirst", "TestLast", address).id();
        //When
        customers.updateCustomer(id, "Updated", "UpdatedLast", address);
        //Then
        assertThat(customers.getCustomer(id).firstName()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("Customer should be deleted")
    void customerShouldBeDeleted() {
        //Given
        Address address = new Address("Hungary", "1095", "Budapest", "Teszt utca 2");
        Long id = customers.createCustomer("ToDelete", "Customer", address).id();
        //When
        customers.deleteCustomer(id);
        //Then
        assertThatThrownBy(() -> customers.getCustomer(id)).isInstanceOf(CustomerNotFoundException.class);
    }
}
