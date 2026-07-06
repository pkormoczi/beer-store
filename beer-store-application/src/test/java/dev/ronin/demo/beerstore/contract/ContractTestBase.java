package dev.ronin.demo.beerstore.contract;

import dev.ronin.demo.beerstore.adapter.in.rest.CustomerController;
import dev.ronin.demo.beerstore.adapter.out.persistence.CustomerJpaRepository;
import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import static dev.ronin.demo.beerstore.BeerStoreApplication.PROFILE_TEST;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles(PROFILE_TEST)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ContractTestBase {

    @LocalServerPort
    int port;

    @MockitoSpyBean
    private CustomerController customerController;

    @Autowired
    private CustomerJpaRepository customerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    @Transactional
    @Step("Seed customer test data and configure RestAssured")
    void setup() {
        final CustomerData customerData = new ContractDataReader().readCustomerData();
        customerData.setId(null);
        // The GET /customers/1 contract pins the id; reset the table so the seeded row is guaranteed to get id 1.
        customerRepository.deleteAll();
        jdbcTemplate.execute("ALTER TABLE customer ALTER COLUMN id RESTART WITH 1");
        customerRepository.save(customerData);
        RestAssured.baseURI = "http://localhost:" + this.port;
        RestAssured.filters(new AllureRestAssured());
    }

}
