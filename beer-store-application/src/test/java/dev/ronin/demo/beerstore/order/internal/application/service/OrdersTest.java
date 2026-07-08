package dev.ronin.demo.beerstore.order.internal.application.service;

import dev.ronin.demo.beerstore.order.api.command.PlaceOrder;
import dev.ronin.demo.beerstore.order.api.exception.UnknownBeerException;
import dev.ronin.demo.beerstore.order.api.type.OrderStatus;
import dev.ronin.demo.beerstore.order.internal.application.port.out.BeerLookup;
import dev.ronin.demo.beerstore.order.internal.application.port.out.BeerSnapshot;
import dev.ronin.demo.beerstore.order.internal.application.port.out.CustomerLookup;
import dev.ronin.demo.beerstore.order.internal.application.port.out.OrderRepository;
import dev.ronin.demo.beerstore.order.internal.domain.model.Order;
import dev.ronin.demo.beerstore.order.internal.domain.model.OrderLine;
import dev.ronin.demo.beerstore.shared.kernel.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrdersTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    BeerLookup beerLookup;

    @Mock
    CustomerLookup customerLookup;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Captor
    ArgumentCaptor<Order> orderArgumentCaptor;

    @Test
    @DisplayName("When creating a new order it should have \"New\" status")
    void whenCreatingNewOrderItHasNewStatus() {
        BeerSnapshot snapshot = new BeerSnapshot(1L, "Csoda IPA", new Money(new BigDecimal("2.50")));
        Order expected = new Order(1L, OrderStatus.NEW, 1L,
                List.of(new OrderLine(1L, "Csoda IPA", new Money(new BigDecimal("2.50")), 1)));
        given(beerLookup.findExisting(List.of(1L))).willReturn(List.of(snapshot));
        given(orderRepository.save(Mockito.any())).willReturn(expected);
        Orders orders = new Orders(orderRepository, beerLookup, customerLookup, eventPublisher);

        Long actual = orders.placeOrder(new PlaceOrder(1L, Collections.singletonList(1L)));

        BDDMockito.then(customerLookup).should().assertCustomerExists(1L);
        BDDMockito.then(orderRepository).should().save(orderArgumentCaptor.capture());
        then(orderArgumentCaptor.getValue().orderStatus()).isEqualTo(OrderStatus.NEW);
        then(actual).isEqualTo(expected.id());
    }

    @Test
    @DisplayName("When an order references an unknown beer it should be rejected")
    void whenOrderReferencesUnknownBeerItIsRejected() {
        given(beerLookup.findExisting(List.of(99L))).willReturn(Collections.emptyList());
        Orders orders = new Orders(orderRepository, beerLookup, customerLookup, eventPublisher);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> orders.placeOrder(new PlaceOrder(1L, Collections.singletonList(99L))))
                .isInstanceOf(UnknownBeerException.class);
    }
}
