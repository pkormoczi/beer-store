package dev.ronin.demo.beerstore.domain.order;

import dev.ronin.demo.beerstore.domain.customer.Customers;
import dev.ronin.demo.beerstore.domain.customer.model.Customer;
import dev.ronin.demo.beerstore.domain.order.model.Beer;
import dev.ronin.demo.beerstore.domain.order.model.Order;
import dev.ronin.demo.beerstore.domain.order.repository.BeerRepository;
import dev.ronin.demo.beerstore.domain.order.repository.OrderRepository;
import dev.ronin.demo.beerstore.domain.order.value.OrderStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class Orders {

    private final OrderRepository orderRepository;
    private final BeerRepository beerRepository;
    private final Customers customers;

    public Orders(OrderRepository orderRepository, BeerRepository beerRepository, Customers customers) {
        this.orderRepository = orderRepository;
        this.beerRepository = beerRepository;
        this.customers = customers;
    }

    @Transactional
    public Long newOrder(Long customerId, List<Long> beerIds) {
        Customer customer = customers.customer(customerId);
        List<Beer> beers = beerRepository.findAllById(beerIds);
        Order order = Order.builder()
                .customer(customer)
                .beers(beers)
                .orderStatus(OrderStatus.NEW)
                .build();
        return orderRepository.save(order).getId();
    }

    public List<Order> list() {
        return orderRepository.findAll();
    }

    public Order order(Long id) {
        return orderRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }
}
