package dev.ronin.demo.beerstore.order.adapter.out.persistence;

import dev.ronin.demo.beerstore.order.domain.model.Order;
import dev.ronin.demo.beerstore.order.domain.model.OrderLine;
import dev.ronin.demo.beerstore.shared.kernel.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * {@code Order}/{@code OrderLine} are self-validating records (an {@code Order} always needs at
 * least one line), so the top-level mapping can't go through MapStruct's usual constructor
 * mapping on the domain side - an intermediate "Order without its lines yet" instance would fail
 * its own invariant before the lines could be attached. Only the flat {@code OrderJpaEntity}
 * fields are MapStruct-generated ({@link #toEntityWithoutLines}); the lines and the
 * parent back-reference are wired by hand in the default methods below.
 */
@Mapper
public interface OrderPersistenceMapper {

    @Mapping(target = "lines", ignore = true)
    @Mapping(target = "line", ignore = true) // Lombok @Singular adder on OrderJpaEntity.lines
    OrderJpaEntity toEntityWithoutLines(Order order);

    default OrderJpaEntity toData(Order order) {
        OrderJpaEntity entity = toEntityWithoutLines(order);
        entity.setLines(order.lines().stream().map(line -> toLineData(line, entity)).toList());
        return entity;
    }

    default OrderLineJpaEntity toLineData(OrderLine line, OrderJpaEntity order) {
        return OrderLineJpaEntity.builder()
                .order(order)
                .beerId(line.beerId())
                .beerNameSnapshot(line.beerNameSnapshot())
                .unitPriceSnapshotAmount(line.unitPriceSnapshot().amount())
                .quantity(line.quantity())
                .build();
    }

    default Order toDomain(OrderJpaEntity data) {
        List<OrderLine> lines = data.getLines().stream().map(OrderPersistenceMapper::toLineDomain).toList();
        return new Order(data.getId(), data.getOrderStatus(), data.getCustomerId(), lines);
    }

    static OrderLine toLineDomain(OrderLineJpaEntity data) {
        return new OrderLine(data.getBeerId(), data.getBeerNameSnapshot(),
                new Money(data.getUnitPriceSnapshotAmount()), data.getQuantity());
    }
}
