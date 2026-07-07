package dev.ronin.demo.beerstore.order.application;

import dev.ronin.demo.beerstore.customer.Customer;
import dev.ronin.demo.beerstore.customer.ManageCustomersUseCase;
import dev.ronin.demo.beerstore.order.Beer;
import dev.ronin.demo.beerstore.order.IllegalOrderStateException;
import dev.ronin.demo.beerstore.order.ManageOrdersUseCase;
import dev.ronin.demo.beerstore.order.Order;
import dev.ronin.demo.beerstore.order.OrderNotFoundException;
import dev.ronin.demo.beerstore.order.OrderPlaced;
import dev.ronin.demo.beerstore.order.OrderStatus;
import dev.ronin.demo.beerstore.order.UnknownBeerException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class Orders implements ManageOrdersUseCase {

    private final OrderRepository orderRepository;
    private final BeerRepository beerRepository;
    private final ManageCustomersUseCase manageCustomersUseCase;
    private final ApplicationEventPublisher eventPublisher;

    public Orders(OrderRepository orderRepository, BeerRepository beerRepository,
                  ManageCustomersUseCase manageCustomersUseCase, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.beerRepository = beerRepository;
        this.manageCustomersUseCase = manageCustomersUseCase;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Long newOrder(Long customerId, List<Long> beerIds) {
        if (beerIds == null || beerIds.isEmpty()) {
            throw new IllegalArgumentException("An order requires at least one beer");
        }
        Customer customer = manageCustomersUseCase.getCustomer(customerId);
        List<Beer> beers = beerRepository.findAllById(beerIds);
        if (beers.size() != beerIds.size()) {
            throw new UnknownBeerException(beerIds);
        }
        Order order = new Order(null, OrderStatus.NEW, customer.id(), beers);
        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderPlaced(saved.id(), saved.customerId()));
        return saved.id();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> list() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Order order(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    @Transactional
    public Order updateStatus(Long id, OrderStatus newStatus) {
        Order existing = order(id);
        if (!existing.orderStatus().canTransitionTo(newStatus)) {
            throw new IllegalOrderStateException(existing.orderStatus(), newStatus);
        }
        Order updated = new Order(existing.id(), newStatus, existing.customerId(), existing.beers());
        return orderRepository.save(updated);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        updateStatus(id, OrderStatus.CANCELLED);
    }
}
