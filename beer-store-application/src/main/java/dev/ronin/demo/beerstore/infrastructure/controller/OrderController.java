package dev.ronin.demo.beerstore.infrastructure.controller;

import dev.ronin.demo.beerstore.infrastructure.adapter.OrdersAdapter;
import dev.ronin.demo.beerstore.infrastructure.data.OrderData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/orders", produces = APPLICATION_JSON_VALUE)
public class OrderController {

    private final OrdersAdapter ordersAdapter;

    public OrderController(OrdersAdapter ordersAdapter) {
        this.ordersAdapter = ordersAdapter;
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<OrderData> getOrderById(@PathVariable final Long id) {
        return ResponseEntity.ok(ordersAdapter.findById(id));
    }

    @GetMapping
    public List<OrderData> getOrders() {
        return ordersAdapter.getOrders();
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> createOrder(@RequestBody OrderData order) {
        return ResponseEntity.ok(ordersAdapter.addOrder(order));
    }
}
