package dev.ronin.demo.beerstore.order;

import java.util.List;

/**
 * Inbound (driving) port for the order aggregate, and the order module's exposed API. Adapters
 * depend on this interface rather than the concrete
 * {@link dev.ronin.demo.beerstore.order.application.Orders} service.
 */
public interface ManageOrdersUseCase {

    Long newOrder(Long customerId, List<Long> beerIds);

    List<Order> list();

    Order order(Long id);

    Order updateStatus(Long id, OrderStatus newStatus);

    void cancelOrder(Long id);
}
