package dev.ronin.demo.beerstore.contract;

import dev.ronin.demo.beerstore.customer.api.view.CustomerView;
import dev.ronin.demo.beerstore.product.api.type.BeerAvailability;
import dev.ronin.demo.beerstore.product.api.type.BeerStyle;
import dev.ronin.demo.beerstore.product.api.view.BeerView;
import dev.ronin.demo.beerstore.shared.api.model.CustomerDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContractDataReaderTest {

    private final ContractDataReader contractDataReader = new ContractDataReader();

    @Test
    @DisplayName("Customer entity is seeded from the contract fixture through the production mapper")
    void readCustomerData() {
        CustomerView customer = contractDataReader.readCustomerData();

        assertThat(customer.firstName()).isEqualTo("TestFirst");
        assertThat(customer.lastName()).isEqualTo("TestLast");
        assertThat(customer.address().city()).isEqualTo("MockCity");
    }

    @Test
    @DisplayName("Beer views are seeded from the catalog contract fixture array")
    void readCatalogData() {
        List<BeerView> beers = contractDataReader.readCatalogData();

        assertThat(beers).hasSize(2);
        assertThat(beers).extracting(BeerView::name).containsExactly("Amber Waves", "Csoda IPA");
        assertThat(beers).extracting(BeerView::beerStyle).containsOnly(BeerStyle.IPA);
        assertThat(beers).extracting(BeerView::availability).containsOnly(BeerAvailability.IN_STOCK);
        assertThat(beers.get(0).price().amount()).isEqualByComparingTo(new BigDecimal("2.50"));
        assertThat(beers.get(1).price().amount()).isEqualByComparingTo(new BigDecimal("3.75"));
    }

    @Test
    @DisplayName("Any contract fixture can be read generically into a given type")
    void readGenericFixture() {
        CustomerDto customerDto = contractDataReader.read("customer/customer", CustomerDto.class);

        assertThat(customerDto.getFirstName()).isEqualTo("TestFirst");
    }

    @Test
    @DisplayName("Missing fixture fails loudly instead of resolving silently")
    void missingFixtureFailsLoudly() {
        assertThatThrownBy(() -> contractDataReader.read("no-such-fixture", CustomerDto.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no-such-fixture");
    }
}
