package dev.ronin.demo.beerstore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureJsonTesters
@AutoConfigureStubRunner(
        stubsMode = StubRunnerProperties.StubsMode.LOCAL,
        ids = "dev.ronin.demo:beer-store-application:1.0.0-SNAPSHOT:stubs:8080")
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
                .isOk();
    }
}
