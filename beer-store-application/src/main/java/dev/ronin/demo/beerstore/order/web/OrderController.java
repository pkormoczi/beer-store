package dev.ronin.demo.beerstore.order.web;

import dev.ronin.demo.beerstore.shared.api.OrderApi;
import dev.ronin.demo.beerstore.shared.api.model.OrderModel;
import dev.ronin.demo.beerstore.shared.api.model.OrderStatusUpdateRequestModel;
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

    @Override
    public ResponseEntity<OrderModel> updateOrderStatus(Long id, OrderStatusUpdateRequestModel orderStatusUpdateRequestModel) {
        return ResponseEntity.ok(ordersAdapter.updateOrderStatus(id, orderStatusUpdateRequestModel.getStatus()));
    }

    @Override
    public ResponseEntity<Void> cancelOrder(Long id) {
        ordersAdapter.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
