package dev.ronin.demo.beerstore.order.internal.adapter.out.persistence;

import dev.ronin.demo.beerstore.order.internal.domain.model.Order;
import org.mapstruct.Mapper;

@Mapper
public interface OrderPersistenceMapper {

    OrderJpaEntity toData(Order order);

    Order toDomain(OrderJpaEntity data);
}
