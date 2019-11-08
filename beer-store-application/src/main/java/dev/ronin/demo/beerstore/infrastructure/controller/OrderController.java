package dev.ronin.demo.beerstore.infrastructure.controller;

import dev.ronin.demo.beerstore.domain.order.Order;
import dev.ronin.demo.beerstore.domain.order.OrderService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping(value = "/{id}")
    public Order getOrderById(@PathVariable final Long id) {
        return orderService.findById(id).orElseThrow(() -> new OrderNotFoundException("Order not found!"));
    }

    @GetMapping(value = "/")
    public List<Order> getOrders() {
        return orderService.getOrders();
    }
}
