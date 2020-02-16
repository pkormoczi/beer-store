package dev.ronin.demo.beerstore;

import dev.ronin.demo.beerstore.contract.customer.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureJsonTesters
@AutoConfigureStubRunner(
        stubsMode = StubRunnerProperties.StubsMode.LOCAL,
        ids = "dev.ronin.demo:beer-store-application:+:stubs:8080")
public class CustomerControllerTest {

    @Test
    public void testCustomerContract() {

        WebTestClient testClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8080")
                .build();
        testClient.get().uri("customers/TestFirst")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Customer.class);
    }
}
