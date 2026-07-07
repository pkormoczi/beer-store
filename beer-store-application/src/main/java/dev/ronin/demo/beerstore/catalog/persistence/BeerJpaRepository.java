package dev.ronin.demo.beerstore.catalog.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BeerJpaRepository extends JpaRepository<BeerData, Long> {
}
