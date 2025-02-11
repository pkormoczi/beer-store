package dev.ronin.demo.client;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class BeerStoreClientApplicationIT {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Test if Spring context can be initialized properly")
    void contextLoads() {
        Assertions.assertThat(context.getId()).isEqualTo("application");
    }

}