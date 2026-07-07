package dev.ronin.demo.beerstore.order.application;

import dev.ronin.demo.beerstore.customer.Customer;
import dev.ronin.demo.beerstore.customer.ManageCustomersUseCase;
import org.springframework.stereotype.Component;

@Component
class CustomerLookupAdapter implements CustomerLookup {

    private final ManageCustomersUseCase manageCustomersUseCase;

    CustomerLookupAdapter(ManageCustomersUseCase manageCustomersUseCase) {
        this.manageCustomersUseCase = manageCustomersUseCase;
    }

    @Override
    public Customer getCustomer(Long customerId) {
        return manageCustomersUseCase.getCustomer(customerId);
    }
}
