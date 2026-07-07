package dev.ronin.demo.beerstore.order.web;

import dev.ronin.demo.beerstore.infrastructure.security.Authorized;
import dev.ronin.demo.beerstore.order.ManageOrdersUseCase;
import dev.ronin.demo.beerstore.order.OrderStatus;
import dev.ronin.demo.beerstore.shared.api.model.OrderModel;
import dev.ronin.demo.beerstore.shared.api.model.OrderStatusModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdersAdapter {

    private final ManageOrdersUseCase manageOrdersUseCase;
    private final OrderMapper orderMapper;

    public OrdersAdapter(ManageOrdersUseCase manageOrdersUseCase, OrderMapper orderMapper) {
        this.manageOrdersUseCase = manageOrdersUseCase;
        this.orderMapper = orderMapper;
    }

    public OrderModel findById(Long id) {
        return orderMapper.data(manageOrdersUseCase.order(id));
    }

    public List<OrderModel> getOrders() {
        return orderMapper.dataList(manageOrdersUseCase.list());
    }

    public Long addOrder(OrderModel order) {
        return manageOrdersUseCase.newOrder(order.getCustomerId(), order.getBeers());
    }

    public OrderModel updateOrderStatus(Long id, OrderStatusModel status) {
        return orderMapper.data(manageOrdersUseCase.updateStatus(id, OrderStatus.valueOf(status.name())));
    }

    @Authorized("ADMIN")
    public void cancelOrder(Long id) {
        manageOrdersUseCase.cancelOrder(id);
    }
}
