package dev.ronin.demo.beerstore.customer.internal.adapter.in.rest;

import dev.ronin.demo.beerstore.customer.api.CustomerNotFoundException;
import dev.ronin.demo.beerstore.customer.api.DeleteCustomerCommand;
import dev.ronin.demo.beerstore.customer.api.FindCustomerByNameQuery;
import dev.ronin.demo.beerstore.customer.api.GetCustomerQuery;
import dev.ronin.demo.beerstore.customer.api.ManageCustomersUseCase;
import dev.ronin.demo.beerstore.customer.api.RegisterCustomerCommand;
import dev.ronin.demo.beerstore.customer.api.UpdateCustomerCommand;
import dev.ronin.demo.beerstore.platform.security.Authorized;
import dev.ronin.demo.beerstore.shared.api.model.CustomerModel;
import dev.ronin.demo.beerstore.shared.contract.customerdata.Customer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomersAdapter {

    private final CustomerMapper customerMapper;
    private final ManageCustomersUseCase manageCustomersUseCase;

    public CustomersAdapter(CustomerMapper customerMapper, ManageCustomersUseCase manageCustomersUseCase) {
        this.customerMapper = customerMapper;
        this.manageCustomersUseCase = manageCustomersUseCase;
    }

    public CustomerModel customerWithName(String name) {
        return customerMapper.toModel(manageCustomersUseCase.findCustomerByName(new FindCustomerByNameQuery(name))
                .orElseThrow(() -> CustomerNotFoundException.byName(name)));
    }

    public Customer customerWithNameForWs(String name) {
        return customerMapper.toWsModel(manageCustomersUseCase.findCustomerByName(new FindCustomerByNameQuery(name))
                .orElseThrow(() -> CustomerNotFoundException.byName(name)));
    }

    public List<CustomerModel> customers() {
        return customerMapper.data(manageCustomersUseCase.listCustomers());
    }

    public CustomerModel customerWithId(Long id) {
        return customerMapper.toModel(manageCustomersUseCase.getCustomer(new GetCustomerQuery(id)));
    }

    public CustomerModel newCustomer(CustomerModel customerModel) {
        return customerMapper.toModel(manageCustomersUseCase.registerCustomer(new RegisterCustomerCommand(
                customerModel.getFirstName(), customerModel.getLastName(),
                customerMapper.toAddress(customerModel.getAddress()))));
    }

    public CustomerModel updateCustomer(Long id, CustomerModel customerModel) {
        return customerMapper.toModel(manageCustomersUseCase.updateCustomer(new UpdateCustomerCommand(id,
                customerModel.getFirstName(), customerModel.getLastName(),
                customerMapper.toAddress(customerModel.getAddress()))));
    }

    @Authorized("ADMIN")
    public void deleteCustomer(Long id) {
        manageCustomersUseCase.deleteCustomer(new DeleteCustomerCommand(id));
    }
}
