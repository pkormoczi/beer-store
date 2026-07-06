package dev.ronin.demo.beerstore.infrastructure.controller;

import dev.ronin.demo.beerstore.infrastructure.adapter.OrdersAdapter;
import dev.ronin.demo.beerstore.infrastructure.api.OrderApi;
import dev.ronin.demo.beerstore.infrastructure.api.model.OrderModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController implements OrderApi {

    private final OrdersAdapter ordersAdapter;

    public OrderController(OrdersAdapter ordersAdapter) {
        this.ordersAdapter = ordersAdapter;
    }

    @Override
    public ResponseEntity<OrderModel> getOrderById(final Long id) {
        return ResponseEntity.ok(ordersAdapter.findById(id));
    }

    @Override
    public ResponseEntity<List<OrderModel>> getOrders() {
        return ResponseEntity.ok(ordersAdapter.getOrders());
    }

    @Override
    public ResponseEntity<Long> createOrder(OrderModel orderModel) {
        return ResponseEntity.ok(ordersAdapter.addOrder(orderModel));
    }
}
