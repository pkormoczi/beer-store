package dev.ronin.demo.beerstore.order.adapter.in.rest;

import dev.ronin.demo.beerstore.shared.api.OrderApi;
import dev.ronin.demo.beerstore.shared.api.model.OrderModel;
import dev.ronin.demo.beerstore.shared.api.model.OrderStatusUpdateRequestModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController implements OrderApi {

    private final OrderRestAdapter orderRestAdapter;

    public OrderController(OrderRestAdapter orderRestAdapter) {
        this.orderRestAdapter = orderRestAdapter;
    }

    @Override
    public ResponseEntity<OrderModel> getOrderById(final Long id) {
        return ResponseEntity.ok(orderRestAdapter.findById(id));
    }

    @Override
    public ResponseEntity<List<OrderModel>> getOrders() {
        return ResponseEntity.ok(orderRestAdapter.getOrders());
    }

    @Override
    public ResponseEntity<Long> createOrder(OrderModel orderModel) {
        return ResponseEntity.ok(orderRestAdapter.addOrder(orderModel));
    }

    @Override
    public ResponseEntity<OrderModel> updateOrderStatus(Long id, OrderStatusUpdateRequestModel orderStatusUpdateRequestModel) {
        return ResponseEntity.ok(orderRestAdapter.updateOrderStatus(id, orderStatusUpdateRequestModel.getStatus()));
    }

    @Override
    public ResponseEntity<Void> cancelOrder(Long id) {
        orderRestAdapter.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
