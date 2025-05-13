package dev.ronin.demo.beerstore.infrastructure.controller;

import dev.ronin.demo.beerstore.infrastructure.adapter.CustomersAdapter;
import dev.ronin.demo.beerstore.infrastructure.api.CustomerApi;
import dev.ronin.demo.beerstore.infrastructure.api.model.CustomerModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.Long.valueOf;
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
    public ResponseEntity<CustomerModel> getCustomerById(Integer id) {
        return ResponseEntity.ok(customersAdapter.customerWithId(valueOf(id)));
    }

    @Override
    public ResponseEntity<CustomerModel> getCustomerByName(String name) {
            return ResponseEntity.ok(customersAdapter.customerWithName(name));
    }
}
