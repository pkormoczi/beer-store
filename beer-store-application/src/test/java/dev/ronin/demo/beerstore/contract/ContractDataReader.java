package dev.ronin.demo.beerstore.contract;

import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import dev.ronin.demo.beerstore.infrastructure.adapter.mapper.CustomerMapper;
import dev.ronin.demo.beerstore.infrastructure.adapter.mapper.CustomerMapperImpl;
import dev.ronin.demo.beerstore.infrastructure.api.model.CustomerModel;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Arrays;

class ContractDataReader {

    private static final String CUSTOMER_FIXTURE_PATTERN = "classpath*:/**/contracts/customer/customer.json";

    private final CustomerMapper customerMapper = new CustomerMapperImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public CustomerData readCustomerData() {
        Resource[] resources = new PathMatchingResourcePatternResolver(getClass().getClassLoader())
                .getResources(CUSTOMER_FIXTURE_PATTERN);
        if (resources.length != 1) {
            throw new IllegalStateException(
                    "Expected exactly one contract fixture matching %s but found %d: %s"
                            .formatted(CUSTOMER_FIXTURE_PATTERN, resources.length, Arrays.toString(resources)));
        }
        try (InputStream inputStream = resources[0].getInputStream()) {
            return customerMapper.toData(objectMapper.readValue(inputStream, CustomerModel.class));
        }
    }
}
