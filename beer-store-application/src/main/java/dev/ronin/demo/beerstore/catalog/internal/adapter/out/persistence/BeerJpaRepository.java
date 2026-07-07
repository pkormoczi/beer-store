package dev.ronin.demo.beerstore.catalog.internal.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BeerJpaRepository extends JpaRepository<BeerJpaEntity, Long> {
}
