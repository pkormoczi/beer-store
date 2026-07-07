package dev.ronin.demo.beerstore.order.persistence;

import dev.ronin.demo.beerstore.order.Beer;
import dev.ronin.demo.beerstore.order.Order;
import org.mapstruct.Mapper;

@Mapper
public interface OrderPersistenceMapper {

    OrderData toData(Order order);

    Order toDomain(OrderData data);

    BeerData toData(Beer beer);

    Beer toDomain(BeerData data);
}
