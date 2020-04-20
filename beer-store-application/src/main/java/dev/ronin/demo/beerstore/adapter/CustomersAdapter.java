package dev.ronin.demo.beerstore.adapter;

import dev.ronin.demo.beerstore.adapter.mapper.CustomerMapper;
import dev.ronin.demo.beerstore.contract.customerdata.CustomerModel;
import dev.ronin.demo.beerstore.domain.customer.Customers;
import org.springframework.stereotype.Service;

@Service
public class CustomersAdapter {

    private final CustomerMapper customerMapper;
    private final Customers customers;


    public CustomersAdapter(CustomerMapper customerMapper, Customers customers) {
        this.customerMapper = customerMapper;
        this.customers = customers;
    }


    public CustomerModel customerWithName(String name) {
        return customerMapper.data(customers.customerWithName(name));
    }
}
