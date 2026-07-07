package dev.ronin.demo.beerstore.customer.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerJpaRepository extends JpaRepository<CustomerData, Long> {

    CustomerData findFirstByFirstNameContainingIgnoreCase(String firstName);
}
