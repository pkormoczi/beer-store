package dev.ronin.demo.beerstore.domain;

import dev.ronin.demo.beerstore.infrastructure.port.ExternalServicePort;
import org.springframework.stereotype.Service;

@Service
public class MyService {

    private final ExternalServicePort externalServicePort;

    public MyService(ExternalServicePort externalServicePort) {
        this.externalServicePort = externalServicePort;
    }

    public String processData() {
        return "Processed " + externalServicePort.fetchData();
    }
}
