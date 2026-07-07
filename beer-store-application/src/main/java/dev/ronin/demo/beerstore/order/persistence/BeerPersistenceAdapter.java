package dev.ronin.demo.beerstore.order.persistence;

import dev.ronin.demo.beerstore.order.Beer;
import dev.ronin.demo.beerstore.order.application.BeerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BeerPersistenceAdapter implements BeerRepository {

    private final BeerJpaRepository jpaRepository;
    private final OrderPersistenceMapper mapper;

    public BeerPersistenceAdapter(BeerJpaRepository jpaRepository, OrderPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Beer> findAllById(List<Long> ids) {
        return jpaRepository.findAllById(ids).stream().map(mapper::toDomain).toList();
    }
}
