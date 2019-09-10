package dev.ronin.demo.beerstore.domain.order;

import dev.ronin.demo.beerstore.domain.customer.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Captor
    ArgumentCaptor<Order> orderArgumentCaptor;

    @Test
    void whenCreatingNewOrderItHasNewStatus() {
        Order order = Order.builder()
                .customer(Customer
                        .builder()
                        .firstName("Test")
                        .lastName("TestLast")
                        .build()
                )
                .beers(getBeers())
                .build();

        orderService.addOrder(order);

        verify(orderRepository).saveAndFlush(orderArgumentCaptor.capture());
        then(orderArgumentCaptor.getValue().getOrderStatus()).isEqualTo(OrderStatus.NEW);
    }

    private List<Beer> getBeers() {
        final List<Beer> beers = new ArrayList<>();
        beers.add(Beer.builder().name("Csoda IPA").beerStyle(BeerStyle.IPA).build());
        beers.add(Beer.builder().name("Csoda APA").beerStyle(BeerStyle.APA).build());
        return beers;
    }
}