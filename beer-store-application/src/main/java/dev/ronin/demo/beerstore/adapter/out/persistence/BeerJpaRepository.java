package dev.ronin.demo.beerstore.adapter.out.persistence;

import dev.ronin.demo.beerstore.domain.order.data.BeerData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeerJpaRepository extends JpaRepository<BeerData, Long> {
}
