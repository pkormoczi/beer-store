package dev.ronin.demo.beerstore.order.adapter.in.rest;

import dev.ronin.demo.beerstore.platform.security.Authorized;
import dev.ronin.demo.beerstore.order.api.OrderManagement;
import dev.ronin.demo.beerstore.order.api.command.CancelOrder;
import dev.ronin.demo.beerstore.order.api.command.PlaceOrder;
import dev.ronin.demo.beerstore.order.api.command.UpdateOrderStatus;
import dev.ronin.demo.beerstore.order.api.query.GetOrder;
import dev.ronin.demo.beerstore.order.api.type.OrderStatus;
import dev.ronin.demo.beerstore.shared.api.model.OrderModel;
import dev.ronin.demo.beerstore.shared.api.model.OrderStatusModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdersAdapter {

    private final OrderManagement orderManagement;
    private final OrderMapper orderMapper;

    public OrdersAdapter(OrderManagement orderManagement, OrderMapper orderMapper) {
        this.orderManagement = orderManagement;
        this.orderMapper = orderMapper;
    }

    public OrderModel findById(Long id) {
        return orderMapper.data(orderManagement.getOrder(new GetOrder(id)));
    }

    public List<OrderModel> getOrders() {
        return orderMapper.dataList(orderManagement.listOrders());
    }

    public Long addOrder(OrderModel order) {
        return orderManagement.placeOrder(new PlaceOrder(order.getCustomerId(), order.getBeers()));
    }

    public OrderModel updateOrderStatus(Long id, OrderStatusModel status) {
        return orderMapper.data(orderManagement.updateOrderStatus(
                new UpdateOrderStatus(id, OrderStatus.valueOf(status.name()))));
    }

    @Authorized("ADMIN")
    public void cancelOrder(Long id) {
        orderManagement.cancelOrder(new CancelOrder(id));
    }
}
