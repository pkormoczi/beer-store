package dev.ronin.demo.beerstore.platform.observability;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestLoggingConfig {
    @Bean
    public RequestResponseLoggingFilter loggingFilter(){
        return new RequestResponseLoggingFilter();
    }
}