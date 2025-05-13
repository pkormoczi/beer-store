package dev.ronin.demo.beerstore.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import dev.ronin.demo.beerstore.infrastructure.adapter.mapper.CustomerMapper;
import dev.ronin.demo.beerstore.infrastructure.adapter.mapper.CustomerMapperImpl;
import dev.ronin.demo.beerstore.infrastructure.api.model.CustomerModel;
import lombok.SneakyThrows;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static java.util.Arrays.stream;

class ContractDataReader {

    private final CustomerMapper customerMapper = new CustomerMapperImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public CustomerData readCustomerData() {
        var resources = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader()).getResources("classpath:/**/contracts/customer/customer.json");
        return customerMapper.toData(objectMapper.readValue(stream(resources).findFirst().orElseThrow().getInputStream(), CustomerModel.class));
    }
}
