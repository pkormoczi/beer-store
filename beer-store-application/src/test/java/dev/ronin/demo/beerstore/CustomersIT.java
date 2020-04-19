package dev.ronin.demo.beerstore;

import dev.ronin.demo.beerstore.base.IntegrationTest;
import dev.ronin.demo.beerstore.domain.customer.Customers;
import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import dev.ronin.demo.beerstore.domain.customer.value.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomersIT extends IntegrationTest {

    @Autowired
    Customers customers;

    @Test
    @DisplayName("Customer should be updated")
    void customerShouldBeUpdated() {
        //Given
        Long id = customers.newCustomer("TestFirst", "TestLast",
                new Address()).data().getId();
        CustomerData updated = new CustomerData(id, "Updated", "UpdatedLast", null);
        //When
        customers.customer(id)
                .update(updated);
        //Then
        assertThat(customers.customer(id).data().getFirstName()).isEqualTo("Updated");
    }
}
