package dev.ronin.demo.beerstore.order.internal.domain.model;

import dev.ronin.demo.beerstore.order.api.IllegalOrderStateException;
import dev.ronin.demo.beerstore.order.api.OrderStatus;
import dev.ronin.demo.beerstore.shared.kernel.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;

class OrderTest {

    private static final OrderLine LINE = new OrderLine(1L, "Csoda IPA", new Money(new BigDecimal("2.50")), 2);

    @Test
    @DisplayName("A newly placed order has no id and is New")
    void placeBuildsNewUnsavedOrder() {
        Order order = Order.place(1L, List.of(LINE));

        then(order.id()).isNull();
        then(order.orderStatus()).isEqualTo(OrderStatus.NEW);
        then(order.customerId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("An order requires at least one line")
    void emptyLinesAreRejected() {
        assertThatThrownBy(() -> Order.place(1L, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A null customer id is rejected")
    void nullCustomerIdIsRejected() {
        assertThatThrownBy(() -> Order.place(null, List.of(LINE)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("A legal transition succeeds")
    void legalTransitionSucceeds() {
        Order order = Order.place(1L, List.of(LINE));

        Order processing = order.transitionTo(OrderStatus.PROCESSING);

        then(processing.orderStatus()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    @DisplayName("An illegal transition is rejected")
    void illegalTransitionIsRejected() {
        Order delivered = new Order(1L, OrderStatus.DELIVERED, 1L, List.of(LINE));

        assertThatThrownBy(() -> delivered.transitionTo(OrderStatus.PROCESSING))
                .isInstanceOf(IllegalOrderStateException.class);
    }

    @Test
    @DisplayName("cancel transitions to Cancelled")
    void cancelTransitionsToCancelled() {
        Order order = Order.place(1L, List.of(LINE));

        Order cancelled = order.cancel();

        then(cancelled.orderStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancel on a non-cancellable order is rejected")
    void cancelOnDeliveredOrderIsRejected() {
        Order delivered = new Order(1L, OrderStatus.DELIVERED, 1L, List.of(LINE));

        assertThatThrownBy(delivered::cancel).isInstanceOf(IllegalOrderStateException.class);
    }

    @Test
    @DisplayName("totalAmount sums every line's total")
    void totalAmountSumsLines() {
        OrderLine other = new OrderLine(2L, "Another Beer", new Money(new BigDecimal("1.00")), 3);
        Order order = Order.place(1L, List.of(LINE, other));

        then(order.totalAmount().amount()).isEqualByComparingTo("8.00"); // 2*2.50 + 3*1.00
    }

    @Test
    @DisplayName("beerIds expands each line's quantity into repeated ids")
    void beerIdsExpandsQuantities() {
        Order order = Order.place(1L, List.of(LINE));

        then(order.beerIds()).containsExactly(1L, 1L);
    }
}
