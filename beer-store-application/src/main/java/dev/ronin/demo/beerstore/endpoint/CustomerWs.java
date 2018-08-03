package dev.ronin.demo.beerstore.endpoint;

import beerstore.ronin.dev.contract.customer.Address;
import beerstore.ronin.dev.contract.customer.Customer;
import beerstore.ronin.dev.contract.customer.GetCustomerRequest;
import beerstore.ronin.dev.contract.customer.GetCustomerResponse;
import dev.ronin.demo.beerstore.adapter.CustomerAdapter;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class CustomerWs {

    private static final String NAMESPACE_URI = "http://beerstore.ronin.dev/contract/customer";

    private final CustomerAdapter customerAdapter;

    public CustomerWs(CustomerAdapter customerAdapter) {
        this.customerAdapter = customerAdapter;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getCustomerRequest")
	@ResponsePayload
	public GetCustomerResponse getCustomer(@RequestPayload GetCustomerRequest request) {
		GetCustomerResponse response = new GetCustomerResponse();
		response.setCustomer(customerAdapter.findCustomer(request.getName()));

		return response;
	}
}
