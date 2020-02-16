package dev.ronin.demo.beerstore.infrastructure.controller;

import dev.ronin.demo.beerstore.infrastructure.adapter.OrderAdapter;
import dev.ronin.demo.beerstore.infrastructure.data.OrderDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/orders", produces = APPLICATION_JSON_VALUE)
public class OrderController {

    private final OrderAdapter orderAdapter;

    public OrderController(OrderAdapter orderAdapter) {
        this.orderAdapter = orderAdapter;
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable final Long id) {
        return ResponseEntity.ok(orderAdapter.findById(id));
    }

    @GetMapping
    public List<OrderDTO> getOrders() {
        return orderAdapter.getOrders();
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO order) {
        return ResponseEntity.ok(orderAdapter.addOrder(order));
    }
}
