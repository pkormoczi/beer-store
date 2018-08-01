package dev.ronin.demo.beerstore.soap.endpoint;

import beerstore.ronin.dev.contract.customer.Address;
import beerstore.ronin.dev.contract.customer.Customer;
import beerstore.ronin.dev.contract.customer.GetCustomerRequest;
import beerstore.ronin.dev.contract.customer.GetCustomerResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class CustomerWs {

    private static final String NAMESPACE_URI = "http://dev.ronin.beerstore/contract/customer";

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "getCustomerRequest")
	@ResponsePayload
	public GetCustomerResponse getCustomer(@RequestPayload GetCustomerRequest request) {
		GetCustomerResponse response = new GetCustomerResponse();
        Customer customer = new Customer();
        customer.setFirstName("Anyád");
        customer.setLastName("AnyádLast");
        customer.setId(5);
        customer.setAddress(new Address());
        response.setCustomer(customer);

		return response;
	}
}
