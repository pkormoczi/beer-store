package dev.ronin.demo.beerstore.order.api;

import java.util.List;

import dev.ronin.demo.beerstore.order.api.command.CancelOrder;
import dev.ronin.demo.beerstore.order.api.command.PlaceOrder;
import dev.ronin.demo.beerstore.order.api.command.UpdateOrderStatus;
import dev.ronin.demo.beerstore.order.api.query.GetOrder;
import dev.ronin.demo.beerstore.order.api.view.OrderView;

/**
 * Inbound (driving) port for the order aggregate, and the order module's exposed API. Works
 * exclusively with command/query/view DTOs; the translation to/from the internal
 * {@link dev.ronin.demo.beerstore.order.internal.domain.model.Order} happens inside
 * {@link dev.ronin.demo.beerstore.order.internal.application.service.Orders}.
 */
public interface OrderManagement {

    Long placeOrder(PlaceOrder command);

    List<OrderView> listOrders();

    OrderView getOrder(GetOrder query);

    OrderView updateOrderStatus(UpdateOrderStatus command);

    void cancelOrder(CancelOrder command);
}
