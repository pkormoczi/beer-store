package dev.ronin.demo.beerstore.infrastructure.adapter;

import dev.ronin.demo.beerstore.domain.order.OrderService;
import dev.ronin.demo.beerstore.infrastructure.data.OrderDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderAdapter {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderAdapter(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }


    public OrderDTO findById(Long id) {
        return orderMapper.toDto(orderService.findById(id));
    }

    public List<OrderDTO> getOrders() {
        return orderMapper.toDtoList(orderService.getOrders());
    }

    public Long addOrder(OrderDTO order) {
        return orderService.addOrder(order.getCustomerId(),order.getBeers());
    }
}
