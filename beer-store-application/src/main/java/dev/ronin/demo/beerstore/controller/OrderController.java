package dev.ronin.demo.beerstore.controller;

import dev.ronin.demo.beerstore.domain.Beer;
import dev.ronin.demo.beerstore.domain.Customer;
import dev.ronin.demo.beerstore.domain.Order;
import dev.ronin.demo.beerstore.domain.value.Address;
import dev.ronin.demo.beerstore.domain.value.BeerStyle;
import dev.ronin.demo.beerstore.service.OrderService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostConstruct
    public void init() {
        orderService.addOrder(createTestOrder());
    }

    @GetMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Order> getOrders() {
        return orderService.getOrders();
    }

    private Order createTestOrder() {
        Order order = new Order();
        order.setCustomer(Customer.builder()
                                  .firstName("Teszt")
                                  .lastName("Jánoska")
                                  .address(new Address("Magyarország", "Budapest", "Váci út 76.", "1133"))
                                  .build());
        order.setBeers(Collections.singletonList(Beer.builder()
                                                     .beerStyle(BeerStyle.IPA)
                                                     .name("Egy IPA")
                                                     .build()));
        return order;
    }

}
