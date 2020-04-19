package dev.ronin.demo.beerstore;

import dev.ronin.demo.beerstore.base.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

public class BeerStoreApplicationIT extends IntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Test if Spring context can be initialized properly")
    void contextLoads() {
        assertThat(context).isNotNull();
    }
}