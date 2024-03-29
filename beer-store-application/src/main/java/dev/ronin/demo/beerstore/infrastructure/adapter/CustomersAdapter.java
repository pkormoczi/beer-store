package dev.ronin.demo.beerstore.infrastructure.adapter;

import dev.ronin.demo.beerstore.infrastructure.adapter.mapper.CustomerMapper;
import dev.ronin.demo.beerstore.contract.customerdata.CustomerModel;
import dev.ronin.demo.beerstore.domain.customer.Customers;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<CustomerModel> customers() {
        return customerMapper.data(customers.list());
    }
}
