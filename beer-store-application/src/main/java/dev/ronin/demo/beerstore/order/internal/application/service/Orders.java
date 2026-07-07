package dev.ronin.demo.beerstore.order.internal.application.service;

import dev.ronin.demo.beerstore.order.api.CancelOrderCommand;
import dev.ronin.demo.beerstore.order.api.GetOrderQuery;
import dev.ronin.demo.beerstore.order.api.IllegalOrderStateException;
import dev.ronin.demo.beerstore.order.api.ManageOrdersUseCase;
import dev.ronin.demo.beerstore.order.api.OrderNotFoundException;
import dev.ronin.demo.beerstore.order.api.OrderStatus;
import dev.ronin.demo.beerstore.order.api.OrderView;
import dev.ronin.demo.beerstore.order.api.PlaceOrderCommand;
import dev.ronin.demo.beerstore.order.api.UnknownBeerException;
import dev.ronin.demo.beerstore.order.api.UpdateOrderStatusCommand;
import dev.ronin.demo.beerstore.order.internal.application.port.out.BeerLookup;
import dev.ronin.demo.beerstore.order.internal.application.port.out.CustomerLookup;
import dev.ronin.demo.beerstore.order.internal.application.port.out.OrderRepository;
import dev.ronin.demo.beerstore.order.internal.domain.event.OrderPlaced;
import dev.ronin.demo.beerstore.order.internal.domain.model.Order;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class Orders implements ManageOrdersUseCase {

    private final OrderRepository orderRepository;
    private final BeerLookup beerLookup;
    private final CustomerLookup customerLookup;
    private final ApplicationEventPublisher eventPublisher;

    public Orders(OrderRepository orderRepository, BeerLookup beerLookup,
                  CustomerLookup customerLookup, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.beerLookup = beerLookup;
        this.customerLookup = customerLookup;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Long placeOrder(PlaceOrderCommand command) {
        if (command.beerIds() == null || command.beerIds().isEmpty()) {
            throw new IllegalArgumentException("An order requires at least one beer");
        }
        customerLookup.assertCustomerExists(command.customerId());
        List<Long> existingBeerIds = beerLookup.findExistingIds(command.beerIds());
        if (existingBeerIds.size() != command.beerIds().size()) {
            throw new UnknownBeerException(command.beerIds());
        }
        Order order = new Order(null, OrderStatus.NEW, command.customerId(), command.beerIds());
        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderPlaced(saved.id(), saved.customerId()));
        return saved.id();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderView> listOrders() {
        return orderRepository.findAll().stream().map(Orders::toView).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderView getOrder(GetOrderQuery query) {
        return toView(findOrThrow(query.id()));
    }

    @Override
    @Transactional
    public OrderView updateOrderStatus(UpdateOrderStatusCommand command) {
        Order existing = findOrThrow(command.id());
        if (!existing.orderStatus().canTransitionTo(command.newStatus())) {
            throw new IllegalOrderStateException(existing.orderStatus(), command.newStatus());
        }
        Order updated = new Order(existing.id(), command.newStatus(), existing.customerId(), existing.beers());
        return toView(orderRepository.save(updated));
    }

    @Override
    @Transactional
    public void cancelOrder(CancelOrderCommand command) {
        updateOrderStatus(new UpdateOrderStatusCommand(command.id(), OrderStatus.CANCELLED));
    }

    private Order findOrThrow(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    private static OrderView toView(Order order) {
        return new OrderView(order.id(), order.orderStatus(), order.customerId(), order.beers());
    }
}
