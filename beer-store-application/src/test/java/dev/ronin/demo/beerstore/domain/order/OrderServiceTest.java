package dev.ronin.demo.beerstore.domain.order;

import dev.ronin.demo.beerstore.domain.customer.CustomerService;
import dev.ronin.demo.beerstore.infrastructure.data.OrderDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    BeerRepository beerRepository;

    @Mock
    CustomerService customerService;

    @Captor
    ArgumentCaptor<Order> orderArgumentCaptor;

    @Test
    void whenCreatingNewOrderItHasNewStatus() {
        Order expected = new Order();
        expected.setId(1L);
        given(orderRepository.save(Mockito.any())).willReturn(expected);
        OrderService orderService = new OrderService(orderRepository, beerRepository, customerService);

        Long actual = orderService.addOrder(1L,Collections.singletonList(1L));

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