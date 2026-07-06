package dev.ronin.demo.beerstore.contract;

import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import dev.ronin.demo.beerstore.infrastructure.api.model.CustomerModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContractDataReaderTest {

    private final ContractDataReader contractDataReader = new ContractDataReader();

    @Test
    @DisplayName("Customer entity is seeded from the contract fixture through the production mapper")
    void readCustomerData() {
        CustomerData customerData = contractDataReader.readCustomerData();

        assertThat(customerData.getFirstName()).isEqualTo("TestFirst");
        assertThat(customerData.getLastName()).isEqualTo("TestLast");
        assertThat(customerData.getAddress().getCity()).isEqualTo("MockCity");
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
