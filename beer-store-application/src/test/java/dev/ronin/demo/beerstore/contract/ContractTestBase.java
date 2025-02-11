package dev.ronin.demo.beerstore.contract;

import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import dev.ronin.demo.beerstore.domain.customer.repository.CustomerRepository;
import dev.ronin.demo.beerstore.domain.customer.value.Address;
import dev.ronin.demo.beerstore.infrastructure.controller.CustomerController;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static dev.ronin.demo.beerstore.BeerStoreApplication.PROFILE_TEST;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles(PROFILE_TEST)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
public class ContractTestBase {

    @MockitoSpyBean
    private CustomerController customerController;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setup() {
        customerRepository.save(new CustomerData("TestFirst","TestLast",
                new Address("MockCountry","1111","MockCity","MockAddress")));
        RestAssuredMockMvc.standaloneSetup(customerController);
    }

}
