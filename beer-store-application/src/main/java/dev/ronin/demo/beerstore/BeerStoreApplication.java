package dev.ronin.demo.beerstore;

import dev.ronin.demo.beerstore.infrastructure.logging.PropertiesLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@SpringBootApplication
@Slf4j
public class BeerStoreApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(BeerStoreApplication.class);
        application.addListeners(new PropertiesLogger());
        application.run(args);
    }
}
