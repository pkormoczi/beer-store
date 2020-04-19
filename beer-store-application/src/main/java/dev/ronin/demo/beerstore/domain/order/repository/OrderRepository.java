package dev.ronin.demo.beerstore.domain.order.repository;

import dev.ronin.demo.beerstore.domain.order.data.OrderData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderData, Long> {
}
