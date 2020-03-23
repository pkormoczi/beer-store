package dev.ronin.demo.beerstore;

import dev.ronin.demo.beerstore.contract.customer.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerPort;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureJsonTesters
@AutoConfigureStubRunner(
        stubsMode = StubRunnerProperties.StubsMode.LOCAL,
        ids = "dev.ronin.demo:beer-store-application:+:stubs")
public class CustomerControllerTest {

    @StubRunnerPort("beer-store-application")
    int port;


    @Test
    public void testCustomerContract() {

        WebTestClient testClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + this.port)
                .build();
        testClient.get().uri("customers/TestFirst")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Customer.class);
    }
}
