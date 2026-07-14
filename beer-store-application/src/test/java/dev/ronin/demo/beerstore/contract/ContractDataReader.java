package dev.ronin.demo.beerstore.contract;

import dev.ronin.demo.beerstore.customer.api.view.CustomerView;
import dev.ronin.demo.beerstore.customer.adapter.in.rest.CustomerMapper;
import dev.ronin.demo.beerstore.customer.adapter.in.rest.CustomerMapperImpl;
import dev.ronin.demo.beerstore.product.api.type.BeerAvailability;
import dev.ronin.demo.beerstore.product.api.type.BeerStyle;
import dev.ronin.demo.beerstore.product.api.view.BeerView;
import dev.ronin.demo.beerstore.shared.api.model.BeerDto;
import dev.ronin.demo.beerstore.shared.api.model.CustomerDto;
import dev.ronin.demo.beerstore.shared.kernel.Money;
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

    public CustomerView readCustomerData() {
        return customerMapper.toView(read("customer/customer", CustomerDto.class));
    }

    /**
     * Reads the catalog contract's fixture (a JSON array of {@link BeerDto}) and maps each entry
     * to a {@link BeerView}. There is no production dto-&gt;view mapper to reuse here (unlike
     * {@link #readCustomerData()}'s {@link CustomerMapper#toView}) - {@code /catalog} is read-only,
     * so {@code CatalogMapper} only ever maps the other direction (domain -&gt; wire).
     */
    public List<BeerView> readCatalogData() {
        return readList("catalog/catalog", BeerDto.class).stream().map(this::toView).toList();
    }

    private BeerView toView(BeerDto dto) {
        return new BeerView(dto.getId(), dto.getName(), BeerStyle.valueOf(dto.getBeerStyle().getValue()),
                dto.getAbv(), new Money(dto.getPrice()),
                BeerAvailability.valueOf(dto.getAvailability().getValue()));
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
