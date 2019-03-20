package dev.ronin.demo.beerstore.domain.customer;

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
                                                .address(new Address("Magyarország", "1095", "Budapest","Lechner Ödön fasor 9."))
                                                .build());
    }

    public Customer findCustomerByName(final String name) {
        return customerRepository.findByFirstNameContainingIgnoreCase(name);
    }
}