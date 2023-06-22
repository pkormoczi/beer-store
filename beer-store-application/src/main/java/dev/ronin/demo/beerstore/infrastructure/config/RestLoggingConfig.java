package dev.ronin.demo.beerstore.infrastructure.config;

import dev.ronin.demo.beerstore.infrastructure.logging.RequestResponseLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestLoggingConfig {
    @Bean
    public RequestResponseLoggingFilter loggingFilter(){
        return new RequestResponseLoggingFilter();
    }
}