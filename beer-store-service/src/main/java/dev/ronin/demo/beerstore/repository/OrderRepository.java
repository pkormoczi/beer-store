package dev.ronin.demo.beerstore.repository;

import dev.ronin.demo.beerstore.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
