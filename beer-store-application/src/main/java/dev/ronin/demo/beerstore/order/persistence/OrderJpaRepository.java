package dev.ronin.demo.beerstore.order.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderData, Long> {
}
