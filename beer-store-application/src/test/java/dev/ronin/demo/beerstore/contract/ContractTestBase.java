package dev.ronin.demo.beerstore.contract;

import dev.ronin.demo.beerstore.contract.customer.Address;
import dev.ronin.demo.beerstore.contract.customer.Customer;
import dev.ronin.demo.beerstore.infrastructure.adapter.CustomerAdapter;
import dev.ronin.demo.beerstore.infrastructure.controller.CustomerController;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

//@SpringBootTest(classes = BeerStoreApplication.class)
@ExtendWith(MockitoExtension.class)
public class ContractTestBase {

    @Mock
    private CustomerAdapter customerAdapter;

    @BeforeEach
    void beforeEach() {
        final Customer customer = new Customer(1, "TestFirst", "TestLast", new Address("MockCountry", "MockCity", "MockAddress", "1111"));
        Mockito.when(this.customerAdapter.findCustomer("TestFirst")).thenReturn(customer);
        RestAssuredMockMvc.standaloneSetup(new CustomerController(this.customerAdapter));
    }

}
