package dev.ronin.demo.beerstore.product.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BeerJpaRepository extends JpaRepository<BeerJpaEntity, Long> {
}
