package dev.ronin.demo.beerstore.order.application.port.out;

import dev.ronin.demo.beerstore.order.domain.model.Order;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository {

    Optional<Order> findById(Long id);

    List<Order> findAll();

    Order save(Order data);
}
