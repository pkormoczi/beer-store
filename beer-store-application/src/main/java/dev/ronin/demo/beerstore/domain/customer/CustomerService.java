package dev.ronin.demo.beerstore.domain.customer;

import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer findCustomerByName(final String name) {
        return customerRepository.findByFirstNameContainingIgnoreCase(name);
    }
}
