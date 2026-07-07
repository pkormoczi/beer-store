package dev.ronin.demo.beerstore.contract;

import dev.ronin.demo.beerstore.customer.api.CustomerView;
import dev.ronin.demo.beerstore.shared.api.model.CustomerModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("Any contract fixture can be read generically into a given type")
    void readGenericFixture() {
        CustomerModel customerModel = contractDataReader.read("customer/customer", CustomerModel.class);

        assertThat(customerModel.getFirstName()).isEqualTo("TestFirst");
    }

    @Test
    @DisplayName("Missing fixture fails loudly instead of resolving silently")
    void missingFixtureFailsLoudly() {
        assertThatThrownBy(() -> contractDataReader.read("no-such-fixture", CustomerModel.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no-such-fixture");
    }
}
