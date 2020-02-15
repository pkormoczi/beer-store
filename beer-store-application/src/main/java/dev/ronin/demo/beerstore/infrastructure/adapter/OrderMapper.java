package dev.ronin.demo.beerstore.infrastructure.adapter;

import dev.ronin.demo.beerstore.domain.order.Beer;
import dev.ronin.demo.beerstore.domain.order.Order;
import dev.ronin.demo.beerstore.infrastructure.data.OrderDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface OrderMapper {

    @Mapping(target = "customerId", source = "source.customer.id")
    @Mapping(target = "beers", expression = "java(extractIds(source.getBeers()))")
    OrderDTO toDto(Order source);

    List<OrderDTO> toDtoList(List<Order> orders);

    default List<Long> extractIds(List<Beer> beers) {
        return beers.stream().map(Beer::getId).collect(Collectors.toList());
    }

}
