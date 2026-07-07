package dev.ronin.demo.beerstore.order.application;

import dev.ronin.demo.beerstore.order.Order;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository {

    Optional<Order> findById(Long id);

    List<Order> findAll();

    Order save(Order data);
}
