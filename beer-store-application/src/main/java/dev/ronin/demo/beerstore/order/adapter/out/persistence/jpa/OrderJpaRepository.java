package dev.ronin.demo.beerstore.order.adapter.out.persistence.jpa;

import dev.ronin.demo.beerstore.order.adapter.out.persistence.jpa.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {
}
