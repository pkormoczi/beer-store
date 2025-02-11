package dev.ronin.demo.beerstore.infrastructure.controller;

import dev.ronin.demo.beerstore.infrastructure.adapter.CustomersAdapter;
import dev.ronin.demo.beerstore.infrastructure.api.CustomerApi;
import dev.ronin.demo.beerstore.infrastructure.api.model.CustomerModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController implements CustomerApi {

    final CustomersAdapter customersAdapter;

    public CustomerController(CustomersAdapter customersAdapter) {
        this.customersAdapter = customersAdapter;
    }

    @Override
    public ResponseEntity<CustomerModel> getCustomerByName(String name) {
        return ResponseEntity.ok(customersAdapter.customerWithName(name));
    }
}
