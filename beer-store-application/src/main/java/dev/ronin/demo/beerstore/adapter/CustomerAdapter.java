package dev.ronin.demo.beerstore.adapter;

import dev.ronin.demo.beerstore.contract.customer.Customer;
import dev.ronin.demo.beerstore.domain.customer.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class CustomerAdapter {

    private final CustomerMapper customerMapper;
    private final CustomerService customerService;


    public CustomerAdapter(CustomerMapper customerMapper, CustomerService customerService) {
        this.customerMapper = customerMapper;
        this.customerService = customerService;
    }


    public Customer findCustomer(String name) {
        return customerMapper.toDto(customerService.findCustomerByName(name));
    }
}
