package dev.ronin.demo.beerstore.contract;

import dev.ronin.demo.beerstore.infrastructure.adapter.CustomersAdapter;
import dev.ronin.demo.beerstore.contract.customerdata.AddressModel;
import dev.ronin.demo.beerstore.contract.customerdata.CustomerModel;
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
    private CustomersAdapter customersAdapter;

    @BeforeEach
    void beforeEach() {
        final CustomerModel customer = new CustomerModel(1, "TestFirst", "TestLast", new AddressModel("MockCountry", "MockCity", "MockAddress", "1111"));
        Mockito.when(this.customersAdapter.customerWithName("TestFirst")).thenReturn(customer);
        RestAssuredMockMvc.standaloneSetup(new CustomerController(this.customersAdapter));
    }

}
