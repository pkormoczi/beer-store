package dev.ronin.demo.beerstore.adapter.mapper;

import dev.ronin.demo.beerstore.domain.order.data.BeerData;
import dev.ronin.demo.beerstore.domain.order.data.OrderData;
import dev.ronin.demo.beerstore.infrastructure.data.OrderModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface OrderMapper {

    @Mapping(target = "customerId", source = "source.customer.id")
    @Mapping(target = "beers", expression = "java(beerIdList(source.getBeers()))")
    OrderModel data(OrderData source);

    List<OrderModel> dataList(List<OrderData> orderData);

    default List<Long> beerIdList(List<BeerData> beerData) {
        return beerData.stream().map(BeerData::getId).collect(Collectors.toList());
    }

}
