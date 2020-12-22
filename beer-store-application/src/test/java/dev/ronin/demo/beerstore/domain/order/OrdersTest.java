package dev.ronin.demo.beerstore.domain.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ronin.demo.beerstore.domain.customer.Customers;
import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import dev.ronin.demo.beerstore.domain.customer.value.Address;
import dev.ronin.demo.beerstore.domain.order.data.BeerData;
import dev.ronin.demo.beerstore.domain.order.data.OrderData;
import dev.ronin.demo.beerstore.domain.order.repository.BeerRepository;
import dev.ronin.demo.beerstore.domain.order.repository.OrderRepository;
import dev.ronin.demo.beerstore.domain.order.value.BeerStyle;
import dev.ronin.demo.beerstore.domain.order.value.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

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
    Customers customers;

    @Captor
    ArgumentCaptor<OrderData> orderArgumentCaptor;

    @Test
    @DisplayName("test json mapping")
    void testJsonMapping() throws JsonProcessingException {
        //Given
        CustomerData customerData = new CustomerData();
        customerData.setFirstName("FirstName");
        customerData.setLastName("LastName");
        customerData.setAddress(new Address("Hungary","1095","Budapest","Lechner Ödön fasor 9."));
        //When
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(customerData));
        //Then

    }

    @Test
    @DisplayName("When creating a new order it should have \"New\" status")
    void whenCreatingNewOrderItHasNewStatus() {
        OrderData expected = new OrderData();
        CustomerData customerData = new CustomerData();
        customerData.setId(1L);
        given(orderRepository.save(Mockito.any())).willReturn(expected);
        given(customers.customer(1L)).willReturn(customers.new Customer(customerData));
        Orders orders = new Orders(orderRepository, beerRepository, customers);

        Long actual = orders.newOrder(1L, Collections.singletonList(1L));

        BDDMockito.then(orderRepository).should().save(orderArgumentCaptor.capture());
        then(orderArgumentCaptor.getValue().getOrderStatus()).isEqualTo(OrderStatus.NEW);
        then(actual).isEqualTo(expected.getId());

    }

    private List<BeerData> getBeers() {
        final List<BeerData> beerData = new ArrayList<>();
        beerData.add(BeerData.builder().name("Csoda IPA").beerStyle(BeerStyle.IPA).build());
        beerData.add(BeerData.builder().name("Csoda APA").beerStyle(BeerStyle.APA).build());
        return beerData;
    }
}