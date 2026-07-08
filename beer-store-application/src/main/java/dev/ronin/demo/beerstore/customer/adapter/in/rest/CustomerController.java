package dev.ronin.demo.beerstore.customer.adapter.in.rest;

import dev.ronin.demo.beerstore.shared.api.CustomerApi;
import dev.ronin.demo.beerstore.shared.api.model.CustomerDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class CustomerController implements CustomerApi {

    final CustomerRestAdapter customerRestAdapter;

    public CustomerController(CustomerRestAdapter customerRestAdapter) {
        this.customerRestAdapter = customerRestAdapter;
    }

    @Override
    public ResponseEntity<CustomerDto> createCustomer(CustomerDto customerDto) {
        return status(CREATED).body(customerRestAdapter.newCustomer(customerDto));
    }

    @Override
    public ResponseEntity<CustomerDto> getCustomerById(Long id) {
        return ResponseEntity.ok(customerRestAdapter.customerWithId(id));
    }

    @Override
    public ResponseEntity<CustomerDto> getCustomerByName(String name) {
        return ResponseEntity.ok(customerRestAdapter.customerWithName(name));
    }

    @Override
    public ResponseEntity<List<CustomerDto>> getCustomers() {
        return ResponseEntity.ok(customerRestAdapter.customers());
    }

    @Override
    public ResponseEntity<CustomerDto> updateCustomer(Long id, CustomerDto customerDto) {
        return ResponseEntity.ok(customerRestAdapter.updateCustomer(id, customerDto));
    }

    @Override
    public ResponseEntity<Void> deleteCustomer(Long id) {
        customerRestAdapter.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
