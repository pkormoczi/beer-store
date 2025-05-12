package dev.ronin.demo.client;

import dev.ronin.demo.beerstore.client.infrastructure.api.model.AddressModel;
import dev.ronin.demo.beerstore.client.infrastructure.api.model.CustomerModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerPort;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureStubRunner(
        stubsMode = StubRunnerProperties.StubsMode.CLASSPATH,
        ids = "dev.ronin.demo:beer-store-contract:+:stubs",
        mappingsOutputFolder = "target/stubs")
class CustomerControllerTest {

    @StubRunnerPort("beer-store-contract")
    int port;

    private static final String BASE_URL_FORMAT = "http://localhost:%d";


    private WebTestClient testClient;

    @BeforeEach
    void setUp() {
        testClient = WebTestClient
                .bindToServer()
                .baseUrl(String.format(BASE_URL_FORMAT, port))
                .build();
    }



    @Test
    void testGetExistingCustomer() {
        final CustomerModel responseBody = testClient.get().uri("/customers/search?name=TestFirst")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CustomerModel.class).returnResult().getResponseBody();

        assertThat(responseBody.getFirstName()).isEqualTo("TestFirst");
    }

    @Test
    void testCreateCustomer() {
        final CustomerModel responseBody = testClient.post().uri("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newCustomer()), CustomerModel.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerModel.class).returnResult().getResponseBody();

        assertThat(responseBody.getId()).isNotNull();
    }

    private CustomerModel newCustomer() {
        CustomerModel customer = new CustomerModel();
        customer.setFirstName("TestFirst");
        customer.setLastName("TestLast");
        customer.setAddress(new AddressModel());
        customer.getAddress().setCountry("MockCountry");
        customer.getAddress().setCity("MockCity");
        customer.getAddress().setStreetAddress("MockAddress");
        customer.getAddress().setZip("1111");
        return customer;
    }
}
