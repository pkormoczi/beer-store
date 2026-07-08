package dev.ronin.demo.beerstore.order.adapter.in.rest;

import dev.ronin.demo.beerstore.order.api.view.OrderView;
import dev.ronin.demo.beerstore.shared.api.model.OrderDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Mapping(target = "status", source = "source.orderStatus")
    OrderDto data(OrderView source);

    List<OrderDto> dataList(List<OrderView> views);

}
