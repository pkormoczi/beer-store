package dev.ronin.demo.beerstore.infrastructure.controller;

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

import java.util.List;

@Api(value = "OrderAPI")
@RestController
@RequestMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @ApiOperation(value = "Get an Order with given ID", response = Order.class)
    @GetMapping(value = "/{id}")
    public Order getOrderById(@ApiParam("Order ID") @PathVariable final Long id) {
        return orderService.findById(id).orElseThrow(() -> new OrderNotFoundException("Order not found!"));
    }

    @ApiOperation(value = "Get all Orders")
    @GetMapping(value = "/")
    public List<Order> getOrders() {
        return orderService.getOrders();
    }
}
