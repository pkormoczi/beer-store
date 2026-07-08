package dev.ronin.demo.beerstore.customer.adapter.out.persistence;

import dev.ronin.demo.beerstore.customer.application.port.out.CustomerRepository;
import dev.ronin.demo.beerstore.customer.domain.model.Customer;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomerPersistenceAdapter implements CustomerRepository {

    private final CustomerJpaRepository jpaRepository;
    private final CustomerPersistenceMapper mapper;

    public CustomerPersistenceAdapter(CustomerJpaRepository jpaRepository, CustomerPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Customer save(Customer data) {
        return mapper.toDomain(jpaRepository.save(mapper.toData(data)));
    }

    @Override
    public Optional<Customer> findByNameContaining(String name) {
        return Optional.ofNullable(jpaRepository.findFirstByFirstNameContainingIgnoreCase(name)).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
