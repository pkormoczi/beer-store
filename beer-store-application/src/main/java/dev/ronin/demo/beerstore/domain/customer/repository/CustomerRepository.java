package dev.ronin.demo.beerstore.domain.customer.repository;

import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository {

    Optional<CustomerData> findById(Long id);

    List<CustomerData> findAll();

    CustomerData save(CustomerData data);

    CustomerData findFirstByFirstNameContainingIgnoreCase(final String firstName);
}
