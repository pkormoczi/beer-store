package dev.ronin.demo.beerstore.order.web;

import dev.ronin.demo.beerstore.order.Order;
import dev.ronin.demo.beerstore.shared.api.model.OrderModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Mapping(target = "status", source = "source.orderStatus")
    OrderModel data(Order source);

    List<OrderModel> dataList(List<Order> orders);

}
