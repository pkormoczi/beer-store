package dev.ronin.demo.beerstore;

import dev.ronin.demo.beerstore.base.IntegrationTest;
import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import dev.ronin.demo.beerstore.domain.customer.repository.CustomerRepository;
import dev.ronin.demo.beerstore.infrastructure.controller.OrderController;
import dev.ronin.demo.beerstore.infrastructure.data.OrderModel;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

public class OrdersIT extends IntegrationTest {

    @Autowired
    OrderController orderController;

    @Autowired
    CustomerRepository customerRepository;

    @Test
    @DisplayName("Create new order")
    void createNewOrder() {
        //Given
        CustomerData newCustomer = new CustomerData();
        newCustomer.setFirstName("First");
        newCustomer.setLastName("Last");
        CustomerData savedCustomer = customerRepository.save(newCustomer);
        OrderModel given = givenOrder(savedCustomer.getId());
        //When
        ResponseEntity<Long> result = orderController.createOrder(given);
        //Then
        BDDAssertions.then(result.getBody()).isEqualTo(1L);
    }

    private OrderModel givenOrder(Long customerId) {
        OrderModel model = new OrderModel();
        model.setCustomerId(customerId);
        model.setBeers(Collections.singletonList(1L));
        return model;
    }

}
