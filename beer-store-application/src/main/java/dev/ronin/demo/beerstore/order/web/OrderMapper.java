package dev.ronin.demo.beerstore.order.web;

import dev.ronin.demo.beerstore.order.Beer;
import dev.ronin.demo.beerstore.order.Order;
import dev.ronin.demo.beerstore.shared.api.model.OrderModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface OrderMapper {

    @Mapping(target = "beers", expression = "java(beerIdList(source.beers()))")
    @Mapping(target = "status", source = "source.orderStatus")
    OrderModel data(Order source);

    List<OrderModel> dataList(List<Order> orders);

    default List<Long> beerIdList(List<Beer> beers) {
        return beers.stream().map(Beer::id).collect(Collectors.toList());
    }

}
