package dev.ronin.demo.beerstore.order.internal.adapter.in.rest;

import dev.ronin.demo.beerstore.platform.security.Authorized;
import dev.ronin.demo.beerstore.order.api.CancelOrderCommand;
import dev.ronin.demo.beerstore.order.api.GetOrderQuery;
import dev.ronin.demo.beerstore.order.api.ManageOrdersUseCase;
import dev.ronin.demo.beerstore.order.api.OrderStatus;
import dev.ronin.demo.beerstore.order.api.PlaceOrderCommand;
import dev.ronin.demo.beerstore.order.api.UpdateOrderStatusCommand;
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
        return orderMapper.data(manageOrdersUseCase.getOrder(new GetOrderQuery(id)));
    }

    public List<OrderModel> getOrders() {
        return orderMapper.dataList(manageOrdersUseCase.listOrders());
    }

    public Long addOrder(OrderModel order) {
        return manageOrdersUseCase.placeOrder(new PlaceOrderCommand(order.getCustomerId(), order.getBeers()));
    }

    public OrderModel updateOrderStatus(Long id, OrderStatusModel status) {
        return orderMapper.data(manageOrdersUseCase.updateOrderStatus(
                new UpdateOrderStatusCommand(id, OrderStatus.valueOf(status.name()))));
    }

    @Authorized("ADMIN")
    public void cancelOrder(Long id) {
        manageOrdersUseCase.cancelOrder(new CancelOrderCommand(id));
    }
}
