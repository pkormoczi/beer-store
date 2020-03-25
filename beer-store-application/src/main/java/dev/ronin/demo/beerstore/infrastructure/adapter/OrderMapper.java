package dev.ronin.demo.beerstore.infrastructure.adapter;

import dev.ronin.demo.beerstore.domain.order.model.Beer;
import dev.ronin.demo.beerstore.domain.order.model.Order;
import dev.ronin.demo.beerstore.infrastructure.data.OrderData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface OrderMapper {

    @Mapping(target = "customerId", source = "source.customer.id")
    @Mapping(target = "beers", expression = "java(beerIdList(source.getBeers()))")
    OrderData data(Order source);

    List<OrderData> dataList(List<Order> orders);

    default List<Long> beerIdList(List<Beer> beers) {
        return beers.stream().map(Beer::getId).collect(Collectors.toList());
    }

}
