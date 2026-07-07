package dev.ronin.demo.beerstore.order.application;

import dev.ronin.demo.beerstore.customer.Customer;
import dev.ronin.demo.beerstore.customer.ManageCustomersUseCase;
import dev.ronin.demo.beerstore.order.Beer;
import dev.ronin.demo.beerstore.order.BeerStyle;
import dev.ronin.demo.beerstore.order.Order;
import dev.ronin.demo.beerstore.order.OrderStatus;
import dev.ronin.demo.beerstore.order.UnknownBeerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrdersTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    BeerRepository beerRepository;

    @Mock
    ManageCustomersUseCase manageCustomersUseCase;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Captor
    ArgumentCaptor<Order> orderArgumentCaptor;

    @Test
    @DisplayName("When creating a new order it should have \"New\" status")
    void whenCreatingNewOrderItHasNewStatus() {
        Order expected = new Order(1L, OrderStatus.NEW, 1L, getBeers().subList(0, 1));
        Customer customer = new Customer(1L, "First", "Last", null);
        given(manageCustomersUseCase.getCustomer(1L)).willReturn(customer);
        given(beerRepository.findAllById(Collections.singletonList(1L))).willReturn(getBeers().subList(0, 1));
        given(orderRepository.save(Mockito.any())).willReturn(expected);
        Orders orders = new Orders(orderRepository, beerRepository, manageCustomersUseCase, eventPublisher);

        Long actual = orders.newOrder(1L, Collections.singletonList(1L));

        BDDMockito.then(orderRepository).should().save(orderArgumentCaptor.capture());
        then(orderArgumentCaptor.getValue().orderStatus()).isEqualTo(OrderStatus.NEW);
        then(actual).isEqualTo(expected.id());
    }

    @Test
    @DisplayName("When an order references an unknown beer it should be rejected")
    void whenOrderReferencesUnknownBeerItIsRejected() {
        Customer customer = new Customer(1L, "First", "Last", null);
        given(manageCustomersUseCase.getCustomer(1L)).willReturn(customer);
        given(beerRepository.findAllById(Collections.singletonList(99L))).willReturn(Collections.emptyList());
        Orders orders = new Orders(orderRepository, beerRepository, manageCustomersUseCase, eventPublisher);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> orders.newOrder(1L, Collections.singletonList(99L)))
                .isInstanceOf(UnknownBeerException.class);
    }

    private List<Beer> getBeers() {
        final List<Beer> beers = new ArrayList<>();
        beers.add(new Beer(1L, "Csoda IPA", BeerStyle.IPA));
        beers.add(new Beer(2L, "Csoda APA", BeerStyle.APA));
        return beers;
    }
}
