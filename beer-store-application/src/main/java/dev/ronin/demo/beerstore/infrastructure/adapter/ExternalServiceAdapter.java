package dev.ronin.demo.beerstore.infrastructure.adapter;

import dev.ronin.demo.beerstore.infrastructure.port.ExternalServicePort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!test")
public class ExternalServiceAdapter implements ExternalServicePort {

    @Override
    public String fetchData() {
        // Call external API or service
//        return externalApi.getData();
        return "externalApi.getData()";
    }
}
