package dev.ronin.demo.beerstore.domain.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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