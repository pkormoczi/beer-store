package dev.ronin.demo.beerstore.customer.persistence;

import dev.ronin.demo.beerstore.base.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.BDDAssertions.then;

@Transactional
class CustomerDataRepositoryIT extends IntegrationTest {
    @Autowired
    private CustomerJpaRepository customerRepository;

    @Test
    @DisplayName("When saving a customer it should get an id")
    void whenSavingACustomerItGetsAnId() {
        CustomerData customerData = getTestCustomer();

        customerRepository.save(customerData);

        then(customerData.getId()).isNotNull();
    }

    @Test
    @DisplayName("When searching a customer by it's name we should have a result")
    void whenSearchByName() {
        final CustomerData testCustomerData = getTestCustomer();
        customerRepository.saveAndFlush(testCustomerData);

        final CustomerData result = customerRepository.findFirstByFirstNameContainingIgnoreCase("Peter");

        then(result.getLastName()).isEqualTo(testCustomerData.getLastName());
    }

    private CustomerData getTestCustomer() {
        return CustomerData.builder()
                .firstName("Peter")
                .lastName("Smith")
                .country("Hungary")
                .zip("1165")
                .city("Budapest")
                .streetAddress("Rutafa utca 11")
                .build();
    }
}
