package dev.ronin.demo.beerstore.customer.internal.adapter.in.rest;

import dev.ronin.demo.beerstore.shared.api.CustomerApi;
import dev.ronin.demo.beerstore.shared.api.model.CustomerModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class CustomerController implements CustomerApi {

    final CustomersAdapter customersAdapter;

    public CustomerController(CustomersAdapter customersAdapter) {
        this.customersAdapter = customersAdapter;
    }

    @Override
    public ResponseEntity<CustomerModel> createCustomer(CustomerModel customerModel) {
        return status(CREATED).body(customersAdapter.newCustomer(customerModel));
    }

    @Override
    public ResponseEntity<CustomerModel> getCustomerById(Long id) {
        return ResponseEntity.ok(customersAdapter.customerWithId(id));
    }

    @Override
    public ResponseEntity<CustomerModel> getCustomerByName(String name) {
        return ResponseEntity.ok(customersAdapter.customerWithName(name));
    }

    @Override
    public ResponseEntity<List<CustomerModel>> getCustomers() {
        return ResponseEntity.ok(customersAdapter.customers());
    }

    @Override
    public ResponseEntity<CustomerModel> updateCustomer(Long id, CustomerModel customerModel) {
        return ResponseEntity.ok(customersAdapter.updateCustomer(id, customerModel));
    }

    @Override
    public ResponseEntity<Void> deleteCustomer(Long id) {
        customersAdapter.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
