package dev.ronin.demo.beerstore.domain.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface CustomerRepository extends JpaRepository<Customer, Long> {

    Customer findByFirstNameContainingIgnoreCase(final String firstName);
}
