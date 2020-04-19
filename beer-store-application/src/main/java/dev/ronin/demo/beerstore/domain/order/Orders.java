package dev.ronin.demo.beerstore.domain.order;

import dev.ronin.demo.beerstore.domain.customer.Customers;
import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import dev.ronin.demo.beerstore.domain.order.data.BeerData;
import dev.ronin.demo.beerstore.domain.order.data.OrderData;
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
        CustomerData customerData = customers.customer(customerId).data();
        List<BeerData> beers = beerRepository.findAllById(beerIds);
        OrderData orderData = OrderData.builder()
                .customer(customerData)
                .beers(beers)
                .orderStatus(OrderStatus.NEW)
                .build();
        return orderRepository.save(orderData).getId();
    }

    public List<OrderData> list() {
        return orderRepository.findAll();
    }

    public OrderData order(Long id) {
        return orderRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }
}
