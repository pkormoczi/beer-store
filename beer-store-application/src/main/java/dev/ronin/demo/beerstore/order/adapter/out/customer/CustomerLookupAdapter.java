package dev.ronin.demo.beerstore.order.adapter.out.customer;

import dev.ronin.demo.beerstore.customer.api.CustomerManagement;
import dev.ronin.demo.beerstore.customer.api.query.GetCustomer;
import dev.ronin.demo.beerstore.order.application.port.out.CustomerLookup;
import org.springframework.stereotype.Component;

@Component
class CustomerLookupAdapter implements CustomerLookup {

    private final CustomerManagement customerManagement;

    CustomerLookupAdapter(CustomerManagement customerManagement) {
        this.customerManagement = customerManagement;
    }

    @Override
    public void assertCustomerExists(Long customerId) {
        customerManagement.getCustomer(new GetCustomer(customerId));
    }
}
