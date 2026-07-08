package dev.ronin.demo.beerstore.order.internal.application.service;

import dev.ronin.demo.beerstore.order.api.OrderManagement;
import dev.ronin.demo.beerstore.order.api.command.CancelOrder;
import dev.ronin.demo.beerstore.order.api.command.PlaceOrder;
import dev.ronin.demo.beerstore.order.api.command.UpdateOrderStatus;
import dev.ronin.demo.beerstore.order.api.exception.OrderNotFoundException;
import dev.ronin.demo.beerstore.order.api.exception.UnknownBeerException;
import dev.ronin.demo.beerstore.order.api.query.GetOrder;
import dev.ronin.demo.beerstore.order.api.view.OrderView;
import dev.ronin.demo.beerstore.order.internal.application.port.out.BeerLookup;
import dev.ronin.demo.beerstore.order.internal.application.port.out.BeerSnapshot;
import dev.ronin.demo.beerstore.order.internal.application.port.out.CustomerLookup;
import dev.ronin.demo.beerstore.order.internal.application.port.out.OrderRepository;
import dev.ronin.demo.beerstore.order.internal.domain.event.OrderPlaced;
import dev.ronin.demo.beerstore.order.internal.domain.model.Order;
import dev.ronin.demo.beerstore.order.internal.domain.model.OrderLine;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class Orders implements OrderManagement {

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
    public Long placeOrder(PlaceOrder command) {
        customerLookup.assertCustomerExists(command.customerId());

        Map<Long, Long> quantitiesByBeerId = command.beerIds().stream()
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));

        List<BeerSnapshot> snapshots = beerLookup.findExisting(List.copyOf(quantitiesByBeerId.keySet()));
        Set<Long> foundIds = snapshots.stream().map(BeerSnapshot::beerId).collect(Collectors.toSet());
        if (!foundIds.containsAll(quantitiesByBeerId.keySet())) {
            throw new UnknownBeerException(command.beerIds());
        }

        List<OrderLine> lines = snapshots.stream()
                .map(snapshot -> new OrderLine(snapshot.beerId(), snapshot.name(), snapshot.price(),
                        quantitiesByBeerId.get(snapshot.beerId()).intValue()))
                .toList();

        Order saved = orderRepository.save(Order.place(command.customerId(), lines));
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
    public OrderView getOrder(GetOrder query) {
        return toView(findOrThrow(query.id()));
    }

    @Override
    @Transactional
    public OrderView updateOrderStatus(UpdateOrderStatus command) {
        Order updated = findOrThrow(command.id()).transitionTo(command.newStatus());
        return toView(orderRepository.save(updated));
    }

    @Override
    @Transactional
    public void cancelOrder(CancelOrder command) {
        Order cancelled = findOrThrow(command.id()).cancel();
        orderRepository.save(cancelled);
    }

    private Order findOrThrow(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    private static OrderView toView(Order order) {
        return new OrderView(order.id(), order.orderStatus(), order.customerId(), order.beerIds());
    }
}
