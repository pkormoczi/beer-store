package dev.ronin.demo.beerstore.infrastructure.adapter;

import dev.ronin.demo.beerstore.contract.customerdata.Customer;
import dev.ronin.demo.beerstore.domain.customer.Customers;
import dev.ronin.demo.beerstore.infrastructure.adapter.mapper.CustomerMapper;
import dev.ronin.demo.beerstore.infrastructure.api.model.CustomerModel;
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
        return customerMapper.toModel(customers.customerWithName(name));
    }

    public Customer customerWithNameForWs(String name) {
        return customerMapper.toWsModel(customers.customerWithName(name));
    }

    public List<CustomerModel> customers() {
        return customerMapper.data(customers.list());
    }

    public CustomerModel customerWithId(Long id) {
        return customerMapper.toModel(customers.customerWithId(id));
    }

    public CustomerModel newCustomer(CustomerModel customerModel) {
        return customerMapper.toModel(customers.newCustomer(customerModel.getFirstName(), customerModel.getLastName(), customerMapper.toAddress(customerModel.getAddress())).data());
    }
}
