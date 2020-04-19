package dev.ronin.demo.beerstore.domain.customer.repository;

import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerData, Long> {

    CustomerData findByFirstNameContainingIgnoreCase(final String firstName);
}
