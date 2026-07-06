package dev.ronin.demo.beerstore.adapter.out.persistence;

import dev.ronin.demo.beerstore.domain.order.data.BeerData;
import dev.ronin.demo.beerstore.domain.order.repository.BeerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BeerPersistenceAdapter implements BeerRepository {

    private final BeerJpaRepository jpaRepository;

    public BeerPersistenceAdapter(BeerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<BeerData> findAllById(List<Long> ids) {
        return jpaRepository.findAllById(ids);
    }
}
