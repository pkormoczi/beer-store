package dev.ronin.demo.beerstore.infrastructure.controller;

import dev.ronin.demo.beerstore.contract.customer.Customer;
import dev.ronin.demo.beerstore.infrastructure.adapter.CustomerAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/customers", produces = APPLICATION_JSON_VALUE)
public class CustomerController {

    final CustomerAdapter customerAdapter;

    public CustomerController(CustomerAdapter customerAdapter) {
        this.customerAdapter = customerAdapter;
    }

    @GetMapping(value = "/{name}")
    public ResponseEntity<Customer> getOrderById(@PathVariable final String name) {
        return ResponseEntity.ok(customerAdapter.findCustomer(name));
    }
}
