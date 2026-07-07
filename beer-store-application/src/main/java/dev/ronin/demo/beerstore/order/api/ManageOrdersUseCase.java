package dev.ronin.demo.beerstore.order.api;

import java.util.List;

/**
 * Inbound (driving) port for the order aggregate, and the order module's exposed API. Works
 * exclusively with Command/Query/View DTOs; the translation to/from the internal
 * {@link dev.ronin.demo.beerstore.order.internal.domain.model.Order} happens inside
 * {@link dev.ronin.demo.beerstore.order.internal.application.service.Orders}.
 */
public interface ManageOrdersUseCase {

    Long placeOrder(PlaceOrderCommand command);

    List<OrderView> listOrders();

    OrderView getOrder(GetOrderQuery query);

    OrderView updateOrderStatus(UpdateOrderStatusCommand command);

    void cancelOrder(CancelOrderCommand command);
}
