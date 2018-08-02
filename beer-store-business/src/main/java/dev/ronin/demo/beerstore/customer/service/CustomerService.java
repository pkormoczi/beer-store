package dev.ronin.demo.beerstore.customer.service;

import dev.ronin.demo.beerstore.customer.repository.CustomerRepository;
import dev.ronin.demo.beerstore.domain.Customer;
import dev.ronin.demo.beerstore.domain.value.Address;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @PostConstruct
    public void init() {
        customerRepository.saveAndFlush(Customer.builder()
                                                .firstName("Teszt")
                                                .lastName("Customer")
                                                .address(new Address("Magyarország", "Budapest", "Lechner Ödön fasor 9.", "1095"))
                                                .build());
    }

    public Customer findCustomerByName(final String name) {
        return customerRepository.findByFirstName(name);
    }
}
