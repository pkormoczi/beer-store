package dev.ronin.demo.beerstore.order.internal.adapter.in.rest;

import dev.ronin.demo.beerstore.base.IntegrationTest;
import dev.ronin.demo.beerstore.product.api.BeerStyle;
import dev.ronin.demo.beerstore.product.api.BeerView;
import dev.ronin.demo.beerstore.product.api.CreateBeerCommand;
import dev.ronin.demo.beerstore.product.api.ManageBeersUseCase;
import dev.ronin.demo.beerstore.customer.api.Address;
import dev.ronin.demo.beerstore.customer.api.CustomerView;
import dev.ronin.demo.beerstore.customer.api.ManageCustomersUseCase;
import dev.ronin.demo.beerstore.customer.api.RegisterCustomerCommand;
import dev.ronin.demo.beerstore.shared.api.model.OrderModel;
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
    ManageCustomersUseCase manageCustomersUseCase;

    @Autowired
    ManageBeersUseCase manageBeersUseCase;

    @Test
    @DisplayName("Create new order")
    void createNewOrder() {
        //Given
        CustomerView savedCustomer = manageCustomersUseCase.registerCustomer(new RegisterCustomerCommand("First", "Last",
                new Address("Hungary", "1095", "Budapest", "Teszt utca 1")));
        BeerView savedBeer = manageBeersUseCase.createBeer(
                new CreateBeerCommand("Csoda IPA", BeerStyle.IPA, new Money(new BigDecimal("2.50"))));
        OrderModel given = givenOrder(savedCustomer.id(), savedBeer.id());
        //When
        ResponseEntity<Long> result = orderController.createOrder(given);
        //Then
        BDDAssertions.then(result.getBody()).isNotNull();
    }

    private OrderModel givenOrder(Long customerId, Long beerId) {
        OrderModel model = new OrderModel();
        model.setCustomerId(customerId);
        model.setBeers(Collections.singletonList(beerId));
        return model;
    }

}
