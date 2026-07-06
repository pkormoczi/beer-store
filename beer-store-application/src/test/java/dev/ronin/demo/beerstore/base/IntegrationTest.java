package dev.ronin.demo.beerstore.base;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfig.class)
public abstract class IntegrationTest {
}
