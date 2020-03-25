package dev.ronin.demo.beerstore.domain.order;

import dev.ronin.demo.beerstore.domain.customer.Customers;
import dev.ronin.demo.beerstore.domain.order.model.Beer;
import dev.ronin.demo.beerstore.domain.order.model.Order;
import dev.ronin.demo.beerstore.domain.order.repository.BeerRepository;
import dev.ronin.demo.beerstore.domain.order.repository.OrderRepository;
import dev.ronin.demo.beerstore.domain.order.value.BeerStyle;
import dev.ronin.demo.beerstore.domain.order.value.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrdersTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    BeerRepository beerRepository;

    @Mock
    Customers customers;

    @Captor
    ArgumentCaptor<Order> orderArgumentCaptor;

    @Test
    void whenCreatingNewOrderItHasNewStatus() {
        Order expected = new Order();
        expected.setId(1L);
        given(orderRepository.save(Mockito.any())).willReturn(expected);
        Orders orders = new Orders(orderRepository, beerRepository, customers);

        Long actual = orders.newOrder(1L,Collections.singletonList(1L));

        verify(orderRepository).save(orderArgumentCaptor.capture());
        then(orderArgumentCaptor.getValue().getOrderStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(actual).isEqualTo(expected.getId());

    }

    private List<Beer> getBeers() {
        final List<Beer> beers = new ArrayList<>();
        beers.add(Beer.builder().name("Csoda IPA").beerStyle(BeerStyle.IPA).build());
        beers.add(Beer.builder().name("Csoda APA").beerStyle(BeerStyle.APA).build());
        return beers;
    }
}