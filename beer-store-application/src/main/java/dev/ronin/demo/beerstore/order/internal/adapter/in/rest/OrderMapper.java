package dev.ronin.demo.beerstore.order.internal.adapter.in.rest;

import dev.ronin.demo.beerstore.order.api.OrderView;
import dev.ronin.demo.beerstore.shared.api.model.OrderModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Mapping(target = "status", source = "source.orderStatus")
    OrderModel data(OrderView source);

    List<OrderModel> dataList(List<OrderView> views);

}
