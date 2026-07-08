package dev.ronin.demo.beerstore.customer.internal.application.service;

import dev.ronin.demo.beerstore.customer.api.CustomerManagement;
import dev.ronin.demo.beerstore.customer.api.command.ActivateCustomer;
import dev.ronin.demo.beerstore.customer.api.command.DeleteCustomer;
import dev.ronin.demo.beerstore.customer.api.command.RegisterCustomer;
import dev.ronin.demo.beerstore.customer.api.command.SuspendCustomer;
import dev.ronin.demo.beerstore.customer.api.command.UpdateCustomer;
import dev.ronin.demo.beerstore.customer.api.exception.CustomerNotFoundException;
import dev.ronin.demo.beerstore.customer.api.query.FindCustomerByName;
import dev.ronin.demo.beerstore.customer.api.query.GetCustomer;
import dev.ronin.demo.beerstore.customer.api.view.CustomerView;
import dev.ronin.demo.beerstore.customer.internal.application.port.out.CustomerRepository;
import dev.ronin.demo.beerstore.customer.internal.domain.model.Customer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class Customers implements CustomerManagement {

    private final CustomerRepository customerRepository;

    public Customers(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public CustomerView registerCustomer(RegisterCustomer command) {
        Customer saved = customerRepository.save(
                Customer.register(command.firstName(), command.lastName(), command.address()));
        return toView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerView getCustomer(GetCustomer query) {
        return toView(findOrThrow(query.id()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomerView> findCustomerByName(FindCustomerByName query) {
        return customerRepository.findByNameContaining(query.name()).map(Customers::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerView> listCustomers() {
        return customerRepository.findAll().stream().map(Customers::toView).toList();
    }

    @Override
    @Transactional
    public CustomerView updateCustomer(UpdateCustomer command) {
        Customer updated = findOrThrow(command.id())
                .updateProfile(command.firstName(), command.lastName(), command.address());
        return toView(customerRepository.save(updated));
    }

    @Override
    @Transactional
    public void deleteCustomer(DeleteCustomer command) {
        findOrThrow(command.id());
        customerRepository.deleteById(command.id());
    }

    @Override
    @Transactional
    public CustomerView suspendCustomer(SuspendCustomer command) {
        Customer suspended = findOrThrow(command.id()).suspend();
        return toView(customerRepository.save(suspended));
    }

    @Override
    @Transactional
    public CustomerView activateCustomer(ActivateCustomer command) {
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
