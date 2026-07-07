package dev.ronin.demo.beerstore.customer.application;

import dev.ronin.demo.beerstore.customer.Address;
import dev.ronin.demo.beerstore.customer.Customer;
import dev.ronin.demo.beerstore.customer.CustomerNotFoundException;
import dev.ronin.demo.beerstore.customer.ManageCustomersUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class Customers implements ManageCustomersUseCase {

    private final CustomerRepository customerRepository;

    public Customers(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findCustomerByName(final String name) {
        return customerRepository.findByNameContaining(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomer(final Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> listCustomers() {
        return customerRepository.findAll();
    }

    @Override
    @Transactional
    public Customer createCustomer(String firstName, String lastName, Address address) {
        return customerRepository.save(new Customer(null, firstName, lastName, address));
    }

    @Override
    @Transactional
    public Customer updateCustomer(Long id, String firstName, String lastName, Address address) {
        getCustomer(id);
        return customerRepository.save(new Customer(id, firstName, lastName, address));
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        getCustomer(id);
        customerRepository.deleteById(id);
    }
}
