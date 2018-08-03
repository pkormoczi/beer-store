package dev.ronin.demo.beerstore.customer.repository;

import dev.ronin.demo.beerstore.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Customer findByFirstNameContainingIgnoreCase(final String firstName);
}
