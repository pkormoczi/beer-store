package dev.ronin.demo.beerstore.infrastructure.controller;

import dev.ronin.demo.beerstore.adapter.CustomersAdapter;
import dev.ronin.demo.beerstore.contract.customerdata.CustomerModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/customers", produces = APPLICATION_JSON_VALUE)
public class CustomerController {

    final CustomersAdapter customersAdapter;

    public CustomerController(CustomersAdapter customersAdapter) {
        this.customersAdapter = customersAdapter;
    }

    @GetMapping(value = "/{name}")
    public ResponseEntity<CustomerModel> getOrderById(@PathVariable final String name) {
        return ResponseEntity.ok(customersAdapter.customerWithName(name));
    }
}
