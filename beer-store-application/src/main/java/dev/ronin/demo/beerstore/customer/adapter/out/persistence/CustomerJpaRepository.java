package dev.ronin.demo.beerstore.customer.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, Long> {

    CustomerJpaEntity findFirstByFirstNameContainingIgnoreCase(String firstName);
}
