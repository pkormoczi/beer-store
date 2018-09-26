package dev.ronin.demo.beerstore.infrastructure.controller;

import dev.ronin.demo.beerstore.domain.customer.Address;
import dev.ronin.demo.beerstore.domain.customer.Customer;
import dev.ronin.demo.beerstore.domain.order.Beer;
import dev.ronin.demo.beerstore.domain.order.BeerStyle;
import dev.ronin.demo.beerstore.domain.order.Order;
import dev.ronin.demo.beerstore.domain.order.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Api(value = "OrderAPI")
@RestController
@RequestMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostConstruct
    public void init() {
        orderService.addOrder(createTestOrder());
    }

    @ApiOperation(value = "Get an Order with given ID", response = Order.class)
    @GetMapping(value = "/{id}")
    public Order getOrderById(@ApiParam("Order ID") @PathVariable final Long id) {
        return orderService.findById(id).orElseThrow(() -> new OrderNotFoundException("Order not found!"));
    }

    @ApiOperation(value = "Get all Orders")
    @GetMapping(value = "/all")
    public List<Order> getOrders() {
        return orderService.getOrders();
    }

    private Order createTestOrder() {
        Order order = new Order();
        order.setCustomer(Customer.builder()
                .firstName("János")
                .lastName("Vitéz")
                .address(new Address("Magyarország", "1133", "Budapest", "Váci út 76."))
                .build());
        order.setBeers(Collections.singletonList(Beer.builder()
                .beerStyle(BeerStyle.IPA)
                .name("Egy IPA")
                .build()));
        return order;
    }

}
