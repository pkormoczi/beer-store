package dev.ronin.demo.beerstore.customer.adapter.out.persistence.jpa;

import dev.ronin.demo.beerstore.customer.adapter.out.persistence.jpa.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, Long> {

    CustomerJpaEntity findFirstByFirstNameContainingIgnoreCase(String firstName);
}
