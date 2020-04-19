package dev.ronin.demo.beerstore.domain.order.repository;

import dev.ronin.demo.beerstore.domain.order.data.BeerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BeerRepository extends JpaRepository<BeerData, Long> {
}
