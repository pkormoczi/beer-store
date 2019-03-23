package dev.ronin.demo.beerstore;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BeerStoreApplicationIT {

    @Test
    @DisplayName("Test if Spring context can be initialized properly")
    public void contextLoads() {
    }

}