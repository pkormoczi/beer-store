package dev.ronin.demo.beerstore.contract;

import dev.ronin.demo.beerstore.base.IntegrationTest;
import dev.ronin.demo.beerstore.customer.api.CustomerManagement;
import dev.ronin.demo.beerstore.customer.api.command.RegisterCustomer;
import dev.ronin.demo.beerstore.customer.api.view.CustomerView;
import dev.ronin.demo.beerstore.customer.adapter.in.rest.CustomerController;
import dev.ronin.demo.beerstore.product.api.BeerManagement;
import dev.ronin.demo.beerstore.product.api.command.CreateBeer;
import dev.ronin.demo.beerstore.product.api.view.BeerView;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ContractTestBase extends IntegrationTest {

    @LocalServerPort
    int port;

    @MockitoSpyBean
    private CustomerController customerController;

    @Autowired
    private CustomerManagement customerManagement;

    @Autowired
    private BeerManagement beerManagement;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    @Step("Seed customer and catalog test data and configure RestAssured")
    void setup() {
        final ContractDataReader reader = new ContractDataReader();
        final CustomerView customer = reader.readCustomerData();
        // The GET /customers/1 and GET /catalog contracts pin ids; reset both tables so the
        // seeded rows are guaranteed to get deterministic ids. No @Transactional here: the
        // server serves GET /catalog over real HTTP in its own transaction, so seed data must be
        // committed before the request runs.
        jdbcTemplate.execute("TRUNCATE TABLE customer RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE beer RESTART IDENTITY CASCADE");
        customerManagement.registerCustomer(
                new RegisterCustomer(customer.firstName(), customer.lastName(), customer.address()));
        for (BeerView beer : reader.readCatalogData()) {
            beerManagement.createBeer(
                    new CreateBeer(beer.name(), beer.beerStyle(), beer.abv(), beer.price(), beer.availability()));
        }
        RestAssured.baseURI = "http://localhost:" + this.port;
        RestAssured.filters(new AllureRestAssured());
    }

}
