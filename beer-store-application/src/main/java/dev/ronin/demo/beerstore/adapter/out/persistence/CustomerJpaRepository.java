package dev.ronin.demo.beerstore.adapter.out.persistence;

import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerJpaRepository extends JpaRepository<CustomerData, Long> {

    CustomerData findFirstByFirstNameContainingIgnoreCase(String firstName);
}
