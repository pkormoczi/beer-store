package dev.ronin.demo.beerstore.order.web;

import dev.ronin.demo.beerstore.base.IntegrationTest;
import dev.ronin.demo.beerstore.customer.Address;
import dev.ronin.demo.beerstore.customer.Customer;
import dev.ronin.demo.beerstore.customer.ManageCustomersUseCase;
import dev.ronin.demo.beerstore.order.BeerStyle;
import dev.ronin.demo.beerstore.order.persistence.BeerData;
import dev.ronin.demo.beerstore.order.persistence.BeerJpaRepository;
import dev.ronin.demo.beerstore.shared.api.model.OrderModel;
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
    ManageCustomersUseCase manageCustomersUseCase;

    @Autowired
    BeerJpaRepository beerJpaRepository;

    @Test
    @DisplayName("Create new order")
    void createNewOrder() {
        //Given
        Customer savedCustomer = manageCustomersUseCase.createCustomer("First", "Last",
                new Address("Hungary", "1095", "Budapest", "Teszt utca 1"));
        BeerData savedBeer = beerJpaRepository.save(BeerData.builder().name("Csoda IPA").beerStyle(BeerStyle.IPA).build());
        OrderModel given = givenOrder(savedCustomer.id(), savedBeer.getId());
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
