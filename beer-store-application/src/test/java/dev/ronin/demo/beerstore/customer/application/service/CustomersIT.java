package dev.ronin.demo.beerstore.customer.application.service;

import dev.ronin.demo.beerstore.base.IntegrationTest;
import dev.ronin.demo.beerstore.customer.api.command.DeleteCustomer;
import dev.ronin.demo.beerstore.customer.api.command.RegisterCustomer;
import dev.ronin.demo.beerstore.customer.api.command.UpdateCustomer;
import dev.ronin.demo.beerstore.customer.api.exception.CustomerNotFoundException;
import dev.ronin.demo.beerstore.customer.api.query.GetCustomer;
import dev.ronin.demo.beerstore.customer.api.type.Address;
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
        Long id = customers.registerCustomer(new RegisterCustomer("TestFirst", "TestLast", address)).id();
        //When
        customers.updateCustomer(new UpdateCustomer(id, "Updated", "UpdatedLast", address));
        //Then
        assertThat(customers.getCustomer(new GetCustomer(id)).firstName()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("Customer should be deleted")
    void customerShouldBeDeleted() {
        //Given
        Address address = new Address("Hungary", "1095", "Budapest", "Teszt utca 2");
        Long id = customers.registerCustomer(new RegisterCustomer("ToDelete", "Customer", address)).id();
        //When
        customers.deleteCustomer(new DeleteCustomer(id));
        //Then
        assertThatThrownBy(() -> customers.getCustomer(new GetCustomer(id)))
                .isInstanceOf(CustomerNotFoundException.class);
    }
}
