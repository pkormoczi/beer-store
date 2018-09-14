package dev.ronin.demo.beerstore.domain.order;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    public void whenCallGetOrdersItWorks() {
        when(orderRepository.findAll()).thenReturn(new ArrayList<>());
        orderService.getOrders();
        verify(orderRepository, times(1)).findAll();
    }
}