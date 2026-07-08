package dev.ronin.demo.beerstore.product.adapter.out.persistence.jpa;

import dev.ronin.demo.beerstore.product.application.port.out.BeerRepository;
import dev.ronin.demo.beerstore.product.domain.model.Beer;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BeerPersistenceAdapter implements BeerRepository {

    private final BeerJpaRepository jpaRepository;
    private final BeerPersistenceMapper mapper;

    public BeerPersistenceAdapter(BeerJpaRepository jpaRepository, BeerPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Beer> findAllById(List<Long> ids) {
        return jpaRepository.findAllById(ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Beer> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Beer> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Beer save(Beer data) {
        return mapper.toDomain(jpaRepository.save(mapper.toData(data)));
    }
}
