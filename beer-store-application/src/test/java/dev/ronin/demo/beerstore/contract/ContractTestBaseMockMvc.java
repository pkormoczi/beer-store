package dev.ronin.demo.beerstore.contract;

import dev.ronin.demo.beerstore.base.IntegrationTest;
import dev.ronin.demo.beerstore.customer.api.CustomerManagement;
import dev.ronin.demo.beerstore.customer.api.command.RegisterCustomer;
import dev.ronin.demo.beerstore.customer.api.view.CustomerView;
import dev.ronin.demo.beerstore.customer.adapter.in.rest.CustomerController;
import io.qameta.allure.Step;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class ContractTestBaseMockMvc extends IntegrationTest {

    @MockitoSpyBean
    private CustomerController customerController;

    @Autowired
    private CustomerManagement customerManagement;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    @Step("Seed customer test data and configure RestAssuredMockMvc")
    void setup() {
        final CustomerView customer = new ContractDataReader().readCustomerData();
        // The GET /customers/1 contract pins the id; reset the table so the seeded row is guaranteed to get id 1.
        // No @Transactional here: MockMvc's standalone setup below serves requests through the
        // real controller outside any test transaction, so seed data must be committed first.
        jdbcTemplate.execute("TRUNCATE TABLE customer RESTART IDENTITY CASCADE");
        customerManagement.registerCustomer(
                new RegisterCustomer(customer.firstName(), customer.lastName(), customer.address()));
        RestAssuredMockMvc.standaloneSetup(customerController);
    }

}
