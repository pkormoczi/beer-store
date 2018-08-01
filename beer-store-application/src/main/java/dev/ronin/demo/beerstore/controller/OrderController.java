package dev.ronin.demo.beerstore.controller;

import dev.ronin.demo.beerstore.domain.Beer;
import dev.ronin.demo.beerstore.domain.Customer;
import dev.ronin.demo.beerstore.domain.Order;
import dev.ronin.demo.beerstore.domain.value.Address;
import dev.ronin.demo.beerstore.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public List<Order> getOrders(){
        orderService.addOrder(createOrder());
        return orderService.getOrders();
    }

    private Order createOrder() {
        Order order = new Order();
        order.setCustomer(new Customer( "Teszt","Jánoska",new Address("Magyarország","Budapest","Váci út 76.","1133")));
        order.setBeers(Collections.singletonList(new Beer("Teszt IPA")));
        return order;
    }

}
