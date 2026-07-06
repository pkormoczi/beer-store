package dev.ronin.demo.beerstore.adapter.out.persistence;

import dev.ronin.demo.beerstore.domain.order.data.OrderData;
import dev.ronin.demo.beerstore.domain.order.repository.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public OrderPersistenceAdapter(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<OrderData> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<OrderData> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public OrderData save(OrderData data) {
        return jpaRepository.save(data);
    }
}
