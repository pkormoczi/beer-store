package dev.ronin.demo.beerstore.domain.order;

import dev.ronin.demo.beerstore.domain.customer.Customer;
import dev.ronin.demo.beerstore.domain.customer.CustomerService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final BeerRepository beerRepository;
    private final CustomerService customerService;

    public OrderService(OrderRepository orderRepository, BeerRepository beerRepository, CustomerService customerService) {
        this.orderRepository = orderRepository;
        this.beerRepository = beerRepository;
        this.customerService = customerService;
    }

    @Transactional
    public Long addOrder(Long customerId, List<Long> beerIds) {
        Customer customer = customerService.findCustomerById(customerId);
        List<Beer> beers = beerRepository.findAllById(beerIds);
        Order order = Order.builder()
                .customer(customer)
                .beers(beers)
                .orderStatus(OrderStatus.NEW)
                .build();
        return orderRepository.save(order).getId();
    }

    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

    public Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow();
    }
}
