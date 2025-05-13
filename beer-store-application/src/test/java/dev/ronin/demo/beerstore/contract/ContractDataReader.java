package dev.ronin.demo.beerstore.contract;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import dev.ronin.demo.beerstore.infrastructure.adapter.mapper.CustomerMapper;
import dev.ronin.demo.beerstore.infrastructure.adapter.mapper.CustomerMapperImpl;
import dev.ronin.demo.beerstore.infrastructure.api.model.CustomerModel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Arrays.stream;

class ContractDataReader {

    private final CustomerMapper customerMapper = new CustomerMapperImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public CustomerData readCustomerData() {
        var resources = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader()).getResources("classpath:/**/contracts/customer/customer.json");
        return customerMapper.toData(objectMapper.readValue(stream(resources).findFirst().orElseThrow().getInputStream(), CustomerModel.class));
    }

    @Test
    void name() {
        CustomerData customerData = new DataFromJson<CustomerData>().fileName("customer").withType(CustomerData.class).get();
        Assertions.assertThat(customerData.getFirstName()).isEqualTo("TestFirst");
    }

    @Slf4j
    private static class DataFromJson<T> {
        private static final String MODEL_FILE_NAME_POSTFIX = "Model";

        private OutMainType outMainType = OutMainType.CLASS;
        private String jsonFileName;
        private Class<?> outClass;

        public DataFromJson<T> fileName(String jsonFileName) {
            this.jsonFileName = jsonFileName;

            return this;
        }

        public DataFromJson<T> withType(Class<?> outClass) {
            this.outClass = outClass;

            return this;
        }

        public DataFromJson<T> asList() {
            this.outMainType = OutMainType.LIST;

            return this;
        }

        public T get() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            String realFileName = Optional.ofNullable(this.jsonFileName).orElse(this.outMainType.fileName(this.outClass));

            try {
                Resource[] resources = new PathMatchingResourcePatternResolver(ContractDataReader.class.getClassLoader())
                        .getResources("classpath:/**/contracts/**/" + realFileName + ".json");

                if (resources.length != 1) {
                    throw new IOException(String.format("Json file not found: %s", realFileName));
                }

                return objectMapper.readValue(new FileReader(resources[0].getFile()), this.outMainType.javaType(objectMapper, this.outClass));
            } catch (IOException e) {
                throw new IllegalStateException(format("Error occurred when trying to deserialize test %s.json file as %s. Original exception: %s", realFileName, this.outMainType, e.getMessage()));
            }
        }

        private enum OutMainType {
            CLASS {
                @Override
                public JavaType javaType(ObjectMapper objectMapper, Class<?> outClass) {
                    return objectMapper.getTypeFactory().constructType(outClass);
                }

                @Override
                public String fileName(Class<?> outClass) {
                    return truncateClassName(outClass.getSimpleName());
                }
            },
            LIST {
                @Override
                public JavaType javaType(ObjectMapper objectMapper, Class<?> outClass) {
                    return objectMapper.getTypeFactory().constructCollectionType(List.class, outClass);
                }

                @Override
                public String fileName(Class<?> outClass) {
                    return truncateClassName(outClass.getSimpleName()) + "s";
                }
            };

            public abstract JavaType javaType(ObjectMapper objectMapper, Class<?> outClass);

            public abstract String fileName(Class<?> outClass);

            private static String truncateClassName(String fileName) {
                return fileName.replace(MODEL_FILE_NAME_POSTFIX, "");
            }
        }
    }
}
