package dev.ronin.demo.beerstore.service;

import dev.ronin.demo.beerstore.domain.Order;
import dev.ronin.demo.beerstore.repository.OrderRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;


    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void addOrder(Order order) {
        orderRepository.saveAndFlush(order);
    }

    public List<Order> getOrders() {
        return orderRepository.findAll();
    }
}
