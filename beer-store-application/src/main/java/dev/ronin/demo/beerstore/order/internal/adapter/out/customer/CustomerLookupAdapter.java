package dev.ronin.demo.beerstore.order.internal.adapter.out.customer;

import dev.ronin.demo.beerstore.customer.api.GetCustomerQuery;
import dev.ronin.demo.beerstore.customer.api.ManageCustomersUseCase;
import dev.ronin.demo.beerstore.order.internal.application.port.out.CustomerLookup;
import org.springframework.stereotype.Component;

@Component
class CustomerLookupAdapter implements CustomerLookup {

    private final ManageCustomersUseCase manageCustomersUseCase;

    CustomerLookupAdapter(ManageCustomersUseCase manageCustomersUseCase) {
        this.manageCustomersUseCase = manageCustomersUseCase;
    }

    @Override
    public void assertCustomerExists(Long customerId) {
        manageCustomersUseCase.getCustomer(new GetCustomerQuery(customerId));
    }
}
