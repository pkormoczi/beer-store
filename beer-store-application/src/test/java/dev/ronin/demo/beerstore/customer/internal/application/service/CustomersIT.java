package dev.ronin.demo.beerstore.customer.internal.application.service;

import dev.ronin.demo.beerstore.base.IntegrationTest;
import dev.ronin.demo.beerstore.customer.api.Address;
import dev.ronin.demo.beerstore.customer.api.CustomerNotFoundException;
import dev.ronin.demo.beerstore.customer.api.DeleteCustomerCommand;
import dev.ronin.demo.beerstore.customer.api.GetCustomerQuery;
import dev.ronin.demo.beerstore.customer.api.RegisterCustomerCommand;
import dev.ronin.demo.beerstore.customer.api.UpdateCustomerCommand;
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
        Long id = customers.registerCustomer(new RegisterCustomerCommand("TestFirst", "TestLast", address)).id();
        //When
        customers.updateCustomer(new UpdateCustomerCommand(id, "Updated", "UpdatedLast", address));
        //Then
        assertThat(customers.getCustomer(new GetCustomerQuery(id)).firstName()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("Customer should be deleted")
    void customerShouldBeDeleted() {
        //Given
        Address address = new Address("Hungary", "1095", "Budapest", "Teszt utca 2");
        Long id = customers.registerCustomer(new RegisterCustomerCommand("ToDelete", "Customer", address)).id();
        //When
        customers.deleteCustomer(new DeleteCustomerCommand(id));
        //Then
        assertThatThrownBy(() -> customers.getCustomer(new GetCustomerQuery(id)))
                .isInstanceOf(CustomerNotFoundException.class);
    }
}
