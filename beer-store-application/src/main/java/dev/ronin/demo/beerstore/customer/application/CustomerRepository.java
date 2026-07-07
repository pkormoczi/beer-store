package dev.ronin.demo.beerstore.customer.application;

import dev.ronin.demo.beerstore.customer.Customer;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository {

    Optional<Customer> findById(Long id);

    List<Customer> findAll();

    Customer save(Customer data);

    Optional<Customer> findByNameContaining(String name);

    void deleteById(Long id);
}
