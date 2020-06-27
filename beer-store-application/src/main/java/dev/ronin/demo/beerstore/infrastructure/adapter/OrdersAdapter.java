package dev.ronin.demo.beerstore.infrastructure.adapter;

import dev.ronin.demo.beerstore.infrastructure.adapter.mapper.OrderMapper;
import dev.ronin.demo.beerstore.domain.order.Orders;
import dev.ronin.demo.beerstore.infrastructure.data.OrderModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdersAdapter {

    private final Orders orders;
    private final OrderMapper orderMapper;

    public OrdersAdapter(Orders orders, OrderMapper orderMapper) {
        this.orders = orders;
        this.orderMapper = orderMapper;
    }


    public OrderModel findById(Long id) {
        return orderMapper.data(orders.order(id));
    }

    public List<OrderModel> getOrders() {
        return orderMapper.dataList(orders.list());
    }

    public Long addOrder(OrderModel order) {
        return orders.newOrder(order.getCustomerId(),order.getBeers());
    }
}
