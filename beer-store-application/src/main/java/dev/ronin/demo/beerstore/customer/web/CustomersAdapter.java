package dev.ronin.demo.beerstore.customer.web;

import dev.ronin.demo.beerstore.customer.CustomerNotFoundException;
import dev.ronin.demo.beerstore.customer.ManageCustomersUseCase;
import dev.ronin.demo.beerstore.infrastructure.security.Authorized;
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
        return customerMapper.toModel(manageCustomersUseCase.findCustomerByName(name)
                .orElseThrow(() -> CustomerNotFoundException.byName(name)));
    }

    public Customer customerWithNameForWs(String name) {
        return customerMapper.toWsModel(manageCustomersUseCase.findCustomerByName(name)
                .orElseThrow(() -> CustomerNotFoundException.byName(name)));
    }

    public List<CustomerModel> customers() {
        return customerMapper.data(manageCustomersUseCase.listCustomers());
    }

    public CustomerModel customerWithId(Long id) {
        return customerMapper.toModel(manageCustomersUseCase.getCustomer(id));
    }

    public CustomerModel newCustomer(CustomerModel customerModel) {
        return customerMapper.toModel(manageCustomersUseCase.createCustomer(
                customerModel.getFirstName(), customerModel.getLastName(),
                customerMapper.toAddress(customerModel.getAddress())));
    }

    public CustomerModel updateCustomer(Long id, CustomerModel customerModel) {
        return customerMapper.toModel(manageCustomersUseCase.updateCustomer(id,
                customerModel.getFirstName(), customerModel.getLastName(),
                customerMapper.toAddress(customerModel.getAddress())));
    }

    @Authorized("ADMIN")
    public void deleteCustomer(Long id) {
        manageCustomersUseCase.deleteCustomer(id);
    }
}
