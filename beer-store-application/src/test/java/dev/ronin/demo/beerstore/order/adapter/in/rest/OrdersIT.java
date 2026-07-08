package dev.ronin.demo.beerstore.order.adapter.in.rest;

import dev.ronin.demo.beerstore.base.IntegrationTest;
import dev.ronin.demo.beerstore.product.api.BeerManagement;
import dev.ronin.demo.beerstore.product.api.command.CreateBeer;
import dev.ronin.demo.beerstore.product.api.type.BeerStyle;
import dev.ronin.demo.beerstore.product.api.view.BeerView;
import dev.ronin.demo.beerstore.customer.api.CustomerManagement;
import dev.ronin.demo.beerstore.customer.api.command.RegisterCustomer;
import dev.ronin.demo.beerstore.customer.api.type.Address;
import dev.ronin.demo.beerstore.customer.api.view.CustomerView;
import dev.ronin.demo.beerstore.shared.api.model.OrderDto;
import dev.ronin.demo.beerstore.shared.kernel.Money;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;

public class OrdersIT extends IntegrationTest {

    @Autowired
    OrderController orderController;

    @Autowired
    CustomerManagement customerManagement;

    @Autowired
    BeerManagement beerManagement;

    @Test
    @DisplayName("Create new order")
    void createNewOrder() {
        //Given
        CustomerView savedCustomer = customerManagement.registerCustomer(new RegisterCustomer("First", "Last",
                new Address("Hungary", "1095", "Budapest", "Teszt utca 1")));
        BeerView savedBeer = beerManagement.createBeer(
                new CreateBeer("Csoda IPA", BeerStyle.IPA, 5.5, new Money(new BigDecimal("2.50"))));
        OrderDto given = givenOrder(savedCustomer.id(), savedBeer.id());
        //When
        ResponseEntity<Long> result = orderController.createOrder(given);
        //Then
        BDDAssertions.then(result.getBody()).isNotNull();
    }

    private OrderDto givenOrder(Long customerId, Long beerId) {
        OrderDto dto = new OrderDto();
        dto.setCustomerId(customerId);
        dto.setBeers(Collections.singletonList(beerId));
        return dto;
    }

}
