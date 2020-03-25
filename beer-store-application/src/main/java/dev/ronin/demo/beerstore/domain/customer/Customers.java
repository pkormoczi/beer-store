package dev.ronin.demo.beerstore.domain.customer;

import dev.ronin.demo.beerstore.domain.customer.model.Customer;
import dev.ronin.demo.beerstore.domain.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class Customers {

    private final CustomerRepository customerRepository;

    public Customers(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer customerWithName(final String name) {
        return customerRepository.findByFirstNameContainingIgnoreCase(name);
    }

    public Customer customer(Long id) {
        return customerRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }
}
