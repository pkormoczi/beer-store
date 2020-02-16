package dev.ronin.demo.beerstore.contract;

import dev.ronin.demo.beerstore.BeerStoreApplication;
import dev.ronin.demo.beerstore.contract.customer.Address;
import dev.ronin.demo.beerstore.contract.customer.Customer;
import dev.ronin.demo.beerstore.infrastructure.adapter.CustomerAdapter;
import dev.ronin.demo.beerstore.infrastructure.controller.CustomerController;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.config.MockMvcConfig;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(classes = BeerStoreApplication.class)
public class ContractTestBase {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CustomerController customerController;

    @MockBean
    private CustomerAdapter customerAdapter;

    @BeforeEach
    void beforeEach() {
        final Customer customer = new Customer(1, "TestFirst", "TestLast", new Address("MockCountry", "MockCity", "MockAddress", "1111"));
        Mockito.when(this.customerAdapter.findCustomer("TestFirst")).thenReturn(customer);
        RestAssuredMockMvc.standaloneSetup(this.customerController);
    }

}
