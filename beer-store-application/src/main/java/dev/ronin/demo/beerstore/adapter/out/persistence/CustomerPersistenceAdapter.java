package dev.ronin.demo.beerstore.adapter.out.persistence;

import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import dev.ronin.demo.beerstore.domain.customer.repository.CustomerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomerPersistenceAdapter implements CustomerRepository {

    private final CustomerJpaRepository jpaRepository;

    public CustomerPersistenceAdapter(CustomerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<CustomerData> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<CustomerData> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public CustomerData save(CustomerData data) {
        return jpaRepository.save(data);
    }

    @Override
    public CustomerData findFirstByFirstNameContainingIgnoreCase(String firstName) {
        return jpaRepository.findFirstByFirstNameContainingIgnoreCase(firstName);
    }
}
