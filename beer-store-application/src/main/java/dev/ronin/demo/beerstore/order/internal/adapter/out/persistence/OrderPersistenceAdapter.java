package dev.ronin.demo.beerstore.order.internal.adapter.out.persistence;

import dev.ronin.demo.beerstore.order.internal.application.port.out.OrderRepository;
import dev.ronin.demo.beerstore.order.internal.domain.model.Order;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderPersistenceMapper mapper;

    public OrderPersistenceAdapter(OrderJpaRepository jpaRepository, OrderPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Order save(Order data) {
        return mapper.toDomain(jpaRepository.save(mapper.toData(data)));
    }
}
