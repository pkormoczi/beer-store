package dev.ronin.demo.beerstore.order.application;

import dev.ronin.demo.beerstore.customer.Customer;
import dev.ronin.demo.beerstore.order.Order;
import dev.ronin.demo.beerstore.order.OrderStatus;
import dev.ronin.demo.beerstore.order.UnknownBeerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
        Order expected = new Order(1L, OrderStatus.NEW, 1L, List.of(1L));
        Customer customer = new Customer(1L, "First", "Last", null);
        given(customerLookup.getCustomer(1L)).willReturn(customer);
        given(beerLookup.findExistingIds(Collections.singletonList(1L))).willReturn(List.of(1L));
        given(orderRepository.save(Mockito.any())).willReturn(expected);
        Orders orders = new Orders(orderRepository, beerLookup, customerLookup, eventPublisher);

        Long actual = orders.newOrder(1L, Collections.singletonList(1L));

        BDDMockito.then(orderRepository).should().save(orderArgumentCaptor.capture());
        then(orderArgumentCaptor.getValue().orderStatus()).isEqualTo(OrderStatus.NEW);
        then(actual).isEqualTo(expected.id());
    }

    @Test
    @DisplayName("When an order references an unknown beer it should be rejected")
    void whenOrderReferencesUnknownBeerItIsRejected() {
        Customer customer = new Customer(1L, "First", "Last", null);
        given(customerLookup.getCustomer(1L)).willReturn(customer);
        given(beerLookup.findExistingIds(Collections.singletonList(99L))).willReturn(Collections.emptyList());
        Orders orders = new Orders(orderRepository, beerLookup, customerLookup, eventPublisher);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> orders.newOrder(1L, Collections.singletonList(99L)))
                .isInstanceOf(UnknownBeerException.class);
    }
}
