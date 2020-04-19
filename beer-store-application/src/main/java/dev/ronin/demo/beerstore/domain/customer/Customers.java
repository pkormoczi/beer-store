package dev.ronin.demo.beerstore.domain.customer;

import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import dev.ronin.demo.beerstore.domain.customer.repository.CustomerRepository;
import dev.ronin.demo.beerstore.domain.customer.value.Address;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class Customers {

    private final CustomerRepository customerRepository;

    public Customers(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerData customerWithName(final String name) {
        return customerRepository.findByFirstNameContainingIgnoreCase(name);
    }

    @Transactional
    public Customer customer(Long id) {
        return new Customer(customerRepository.findById(id)
                .orElseThrow(NoSuchElementException::new));
    }

    public List<CustomerData> list() {
        return customerRepository.findAll();
    }

    public Customer newCustomer(String firstName, String lastName, Address address) {
        return new Customer(customerRepository.save(
                new CustomerData(null, firstName, lastName, address)));
    }

    public class Customer {

        private final CustomerData data;

        public Customer(CustomerData data) {
            this.data = data;
        }

        public Customer changeAddress(Address newAddress) {
            this.data.setAddress(newAddress);
            return this;
        }

        public Customer update(CustomerData updated) {
            this.data.setAddress(updated.getAddress());
            this.data.setFirstName(updated.getFirstName());
            this.data.setLastName(updated.getLastName());
            customerRepository.save(this.data);
            return this;
        }

        public CustomerData data() {
            return data;
        }
    }

}
