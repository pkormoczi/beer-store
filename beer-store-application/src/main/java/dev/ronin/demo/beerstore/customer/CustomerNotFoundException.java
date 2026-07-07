package dev.ronin.demo.beerstore.customer;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long id) {
        super("Customer not found: id=" + id);
    }

    private CustomerNotFoundException(String message) {
        super(message);
    }

    public static CustomerNotFoundException byName(String name) {
        return new CustomerNotFoundException("Customer not found: name~" + name);
    }
}
