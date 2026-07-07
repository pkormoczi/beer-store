package dev.ronin.demo.beerstore.customer.internal.application.port.out;

import dev.ronin.demo.beerstore.customer.internal.domain.model.Customer;
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
