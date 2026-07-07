package dev.ronin.demo.beerstore.customer.internal.application.service;

import dev.ronin.demo.beerstore.customer.api.ActivateCustomerCommand;
import dev.ronin.demo.beerstore.customer.api.CustomerNotFoundException;
import dev.ronin.demo.beerstore.customer.api.CustomerView;
import dev.ronin.demo.beerstore.customer.api.DeleteCustomerCommand;
import dev.ronin.demo.beerstore.customer.api.FindCustomerByNameQuery;
import dev.ronin.demo.beerstore.customer.api.GetCustomerQuery;
import dev.ronin.demo.beerstore.customer.api.ManageCustomersUseCase;
import dev.ronin.demo.beerstore.customer.api.RegisterCustomerCommand;
import dev.ronin.demo.beerstore.customer.api.SuspendCustomerCommand;
import dev.ronin.demo.beerstore.customer.api.UpdateCustomerCommand;
import dev.ronin.demo.beerstore.customer.internal.application.port.out.CustomerRepository;
import dev.ronin.demo.beerstore.customer.internal.domain.model.Customer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class Customers implements ManageCustomersUseCase {

    private final CustomerRepository customerRepository;

    public Customers(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public CustomerView registerCustomer(RegisterCustomerCommand command) {
        Customer saved = customerRepository.save(
                Customer.register(command.firstName(), command.lastName(), command.address()));
        return toView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerView getCustomer(GetCustomerQuery query) {
        return toView(findOrThrow(query.id()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomerView> findCustomerByName(FindCustomerByNameQuery query) {
        return customerRepository.findByNameContaining(query.name()).map(Customers::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerView> listCustomers() {
        return customerRepository.findAll().stream().map(Customers::toView).toList();
    }

    @Override
    @Transactional
    public CustomerView updateCustomer(UpdateCustomerCommand command) {
        Customer updated = findOrThrow(command.id())
                .updateProfile(command.firstName(), command.lastName(), command.address());
        return toView(customerRepository.save(updated));
    }

    @Override
    @Transactional
    public void deleteCustomer(DeleteCustomerCommand command) {
        findOrThrow(command.id());
        customerRepository.deleteById(command.id());
    }

    @Override
    @Transactional
    public CustomerView suspendCustomer(SuspendCustomerCommand command) {
        Customer suspended = findOrThrow(command.id()).suspend();
        return toView(customerRepository.save(suspended));
    }

    @Override
    @Transactional
    public CustomerView activateCustomer(ActivateCustomerCommand command) {
        Customer activated = findOrThrow(command.id()).activate();
        return toView(customerRepository.save(activated));
    }

    private Customer findOrThrow(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
    }

    private static CustomerView toView(Customer customer) {
        return new CustomerView(customer.id(), customer.firstName(), customer.lastName(),
                customer.address(), customer.status());
    }
}
