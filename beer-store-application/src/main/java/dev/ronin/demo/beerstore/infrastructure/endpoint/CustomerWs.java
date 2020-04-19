package dev.ronin.demo.beerstore.infrastructure.endpoint;

import dev.ronin.demo.beerstore.contract.customer.GetCustomerRequest;
import dev.ronin.demo.beerstore.contract.customer.GetCustomerResponse;
import dev.ronin.demo.beerstore.adapter.CustomersAdapter;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class CustomerWs {

    private static final String NAMESPACE_URI = "http://demo.ronin.dev/beerstore/contract/customer";

    private final CustomersAdapter customersAdapter;

    public CustomerWs(CustomersAdapter customersAdapter) {
        this.customersAdapter = customersAdapter;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getCustomerRequest")
    @ResponsePayload
    public GetCustomerResponse getCustomer(@RequestPayload GetCustomerRequest request) {
        return new GetCustomerResponse(customersAdapter.customerWithName(request.getName()));
    }
}
