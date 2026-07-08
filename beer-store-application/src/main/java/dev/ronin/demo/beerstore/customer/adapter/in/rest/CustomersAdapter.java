package dev.ronin.demo.beerstore.customer.adapter.in.rest;

import dev.ronin.demo.beerstore.customer.api.CustomerManagement;
import dev.ronin.demo.beerstore.customer.api.command.DeleteCustomer;
import dev.ronin.demo.beerstore.customer.api.command.RegisterCustomer;
import dev.ronin.demo.beerstore.customer.api.command.UpdateCustomer;
import dev.ronin.demo.beerstore.customer.api.exception.CustomerNotFoundException;
import dev.ronin.demo.beerstore.customer.api.query.FindCustomerByName;
import dev.ronin.demo.beerstore.customer.api.query.GetCustomer;
import dev.ronin.demo.beerstore.platform.security.Authorized;
import dev.ronin.demo.beerstore.shared.api.model.CustomerModel;
import dev.ronin.demo.beerstore.shared.contract.customerdata.Customer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomersAdapter {

    private final CustomerMapper customerMapper;
    private final CustomerManagement customerManagement;

    public CustomersAdapter(CustomerMapper customerMapper, CustomerManagement customerManagement) {
        this.customerMapper = customerMapper;
        this.customerManagement = customerManagement;
    }

    public CustomerModel customerWithName(String name) {
        return customerMapper.toModel(customerManagement.findCustomerByName(new FindCustomerByName(name))
                .orElseThrow(() -> CustomerNotFoundException.byName(name)));
    }

    public Customer customerWithNameForWs(String name) {
        return customerMapper.toWsModel(customerManagement.findCustomerByName(new FindCustomerByName(name))
                .orElseThrow(() -> CustomerNotFoundException.byName(name)));
    }

    public List<CustomerModel> customers() {
        return customerMapper.data(customerManagement.listCustomers());
    }

    public CustomerModel customerWithId(Long id) {
        return customerMapper.toModel(customerManagement.getCustomer(new GetCustomer(id)));
    }

    public CustomerModel newCustomer(CustomerModel customerModel) {
        return customerMapper.toModel(customerManagement.registerCustomer(new RegisterCustomer(
                customerModel.getFirstName(), customerModel.getLastName(),
                customerMapper.toAddress(customerModel.getAddress()))));
    }

    public CustomerModel updateCustomer(Long id, CustomerModel customerModel) {
        return customerMapper.toModel(customerManagement.updateCustomer(new UpdateCustomer(id,
                customerModel.getFirstName(), customerModel.getLastName(),
                customerMapper.toAddress(customerModel.getAddress()))));
    }

    @Authorized("ADMIN")
    public void deleteCustomer(Long id) {
        customerManagement.deleteCustomer(new DeleteCustomer(id));
    }
}
