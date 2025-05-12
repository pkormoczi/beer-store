package dev.ronin.demo.client;

import dev.ronin.demo.beerstore.client.infrastructure.api.model.CustomerModel;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerPort;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE)
@AutoConfigureStubRunner(
        stubsMode = StubRunnerProperties.StubsMode.CLASSPATH,
        ids = "dev.ronin.demo:beer-store-contract:+:stubs",
        mappingsOutputFolder = "target/stubs")
class CustomerControllerTest {

    @StubRunnerPort("beer-store-contract")
    int port;


    @Test
    void testCustomerContract() {

        WebTestClient testClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + this.port)
                .build();
        final CustomerModel responseBody = testClient.get().uri("/customers/search?name=TestFirst")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CustomerModel.class).returnResult().getResponseBody();
        Assertions.assertThat(responseBody.getFirstName()).isEqualTo("TestFirst");

    }
}
