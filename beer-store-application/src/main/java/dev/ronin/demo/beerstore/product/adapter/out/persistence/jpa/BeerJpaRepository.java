package dev.ronin.demo.beerstore.product.adapter.out.persistence.jpa;

import dev.ronin.demo.beerstore.product.adapter.out.persistence.jpa.entity.BeerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BeerJpaRepository extends JpaRepository<BeerJpaEntity, Long>, JpaSpecificationExecutor<BeerJpaEntity> {
}
