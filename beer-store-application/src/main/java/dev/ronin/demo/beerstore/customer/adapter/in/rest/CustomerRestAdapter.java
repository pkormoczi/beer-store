package dev.ronin.demo.beerstore.customer.adapter.in.rest;

import dev.ronin.demo.beerstore.customer.api.CustomerManagement;
import dev.ronin.demo.beerstore.customer.api.command.DeleteCustomer;
import dev.ronin.demo.beerstore.customer.api.command.RegisterCustomer;
import dev.ronin.demo.beerstore.customer.api.command.UpdateCustomer;
import dev.ronin.demo.beerstore.customer.api.exception.CustomerNotFoundException;
import dev.ronin.demo.beerstore.customer.api.query.FindCustomerByName;
import dev.ronin.demo.beerstore.customer.api.query.GetCustomer;
import dev.ronin.demo.beerstore.platform.security.Authorized;
import dev.ronin.demo.beerstore.shared.api.model.CustomerDto;
import dev.ronin.demo.beerstore.shared.contract.customerdata.Customer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerRestAdapter {

    private final CustomerMapper customerMapper;
    private final CustomerManagement customerManagement;

    public CustomerRestAdapter(CustomerMapper customerMapper, CustomerManagement customerManagement) {
        this.customerMapper = customerMapper;
        this.customerManagement = customerManagement;
    }

    public CustomerDto customerWithName(String name) {
        return customerMapper.toDto(customerManagement.findCustomerByName(new FindCustomerByName(name))
                .orElseThrow(() -> CustomerNotFoundException.byName(name)));
    }

    public Customer customerWithNameForWs(String name) {
        return customerMapper.toWsModel(customerManagement.findCustomerByName(new FindCustomerByName(name))
                .orElseThrow(() -> CustomerNotFoundException.byName(name)));
    }

    public List<CustomerDto> customers() {
        return customerMapper.data(customerManagement.listCustomers());
    }

    public CustomerDto customerWithId(Long id) {
        return customerMapper.toDto(customerManagement.getCustomer(new GetCustomer(id)));
    }

    public CustomerDto newCustomer(CustomerDto customerDto) {
        return customerMapper.toDto(customerManagement.registerCustomer(new RegisterCustomer(
                customerDto.getFirstName(), customerDto.getLastName(),
                customerMapper.toAddress(customerDto.getAddress()))));
    }

    public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
        return customerMapper.toDto(customerManagement.updateCustomer(new UpdateCustomer(id,
                customerDto.getFirstName(), customerDto.getLastName(),
                customerMapper.toAddress(customerDto.getAddress()))));
    }

    @Authorized("ADMIN")
    public void deleteCustomer(Long id) {
        customerManagement.deleteCustomer(new DeleteCustomer(id));
    }
}
