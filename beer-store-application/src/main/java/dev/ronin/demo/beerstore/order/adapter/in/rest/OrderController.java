package dev.ronin.demo.beerstore.order.adapter.in.rest;

import dev.ronin.demo.beerstore.shared.api.OrderApi;
import dev.ronin.demo.beerstore.shared.api.model.OrderDto;
import dev.ronin.demo.beerstore.shared.api.model.OrderStatusUpdateRequestDto;
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
    public ResponseEntity<OrderDto> getOrderById(final Long id) {
        return ResponseEntity.ok(orderRestAdapter.findById(id));
    }

    @Override
    public ResponseEntity<List<OrderDto>> getOrders() {
        return ResponseEntity.ok(orderRestAdapter.getOrders());
    }

    @Override
    public ResponseEntity<Long> createOrder(OrderDto orderDto) {
        return ResponseEntity.ok(orderRestAdapter.addOrder(orderDto));
    }

    @Override
    public ResponseEntity<OrderDto> updateOrderStatus(Long id, OrderStatusUpdateRequestDto orderStatusUpdateRequestDto) {
        return ResponseEntity.ok(orderRestAdapter.updateOrderStatus(id, orderStatusUpdateRequestDto.getStatus()));
    }

    @Override
    public ResponseEntity<Void> cancelOrder(Long id) {
        orderRestAdapter.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
