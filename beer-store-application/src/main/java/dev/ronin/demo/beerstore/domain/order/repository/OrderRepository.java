package dev.ronin.demo.beerstore.domain.order.repository;

import dev.ronin.demo.beerstore.domain.order.data.OrderData;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository {

    Optional<OrderData> findById(Long id);

    List<OrderData> findAll();

    OrderData save(OrderData data);
}
