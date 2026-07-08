package dev.ronin.demo.beerstore.order.application.service;

import dev.ronin.demo.beerstore.order.domain.event.OrderPlaced;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Demonstrates the event-driven side of the hybrid customer/order integration: reacts to
 * {@link OrderPlaced} asynchronously, through the JPA event publication registry, rather than
 * synchronously in the same transaction as {@link Orders#placeOrder}.
 */
@Slf4j
@Component
public class OrderPlacedEventListener {

    @ApplicationModuleListener
    public void on(OrderPlaced event) {
        log.info("Order {} placed for customer {}", event.orderId(), event.customerId());
    }
}
