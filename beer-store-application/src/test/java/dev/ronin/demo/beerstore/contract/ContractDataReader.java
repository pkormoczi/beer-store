package dev.ronin.demo.beerstore.contract;

import dev.ronin.demo.beerstore.adapter.in.mapper.CustomerMapper;
import dev.ronin.demo.beerstore.adapter.in.mapper.CustomerMapperImpl;
import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import dev.ronin.demo.beerstore.infrastructure.api.model.CustomerModel;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

class ContractDataReader {

    private static final String CONTRACT_FIXTURE_PATTERN = "classpath*:/**/contracts/**/%s.json";

    private final CustomerMapper customerMapper = new CustomerMapperImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomerData readCustomerData() {
        return customerMapper.toData(read("customer/customer", CustomerModel.class));
    }

    /**
     * Reads a single object from a contract fixture JSON. The file name may include a
     * subpath below the contracts directory (e.g. {@code "customer/customer"}) to
     * disambiguate same-named fixtures of different scenarios.
     */
    @SneakyThrows
    public <T> T read(String fileName, Class<T> type) {
        try (InputStream inputStream = resolveFixture(fileName)) {
            return objectMapper.readValue(inputStream, type);
        }
    }

    /**
     * Reads a JSON array from a contract fixture into a list of the given element type.
     */
    @SneakyThrows
    public <T> List<T> readList(String fileName, Class<T> elementType) {
        try (InputStream inputStream = resolveFixture(fileName)) {
            return objectMapper.readValue(inputStream,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
        }
    }

    @SneakyThrows
    private InputStream resolveFixture(String fileName) {
        String pattern = CONTRACT_FIXTURE_PATTERN.formatted(fileName);
        Resource[] resources = new PathMatchingResourcePatternResolver(getClass().getClassLoader())
                .getResources(pattern);
        if (resources.length != 1) {
            throw new IllegalStateException(
                    "Expected exactly one contract fixture matching %s but found %d: %s"
                            .formatted(pattern, resources.length, Arrays.toString(resources)));
        }
        return resources[0].getInputStream();
    }
}
