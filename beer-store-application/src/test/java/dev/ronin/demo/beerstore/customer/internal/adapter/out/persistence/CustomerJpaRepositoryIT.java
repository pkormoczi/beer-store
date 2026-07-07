package dev.ronin.demo.beerstore.customer.internal.adapter.out.persistence;

import dev.ronin.demo.beerstore.base.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.BDDAssertions.then;

@Transactional
class CustomerJpaRepositoryIT extends IntegrationTest {
    @Autowired
    private CustomerJpaRepository customerRepository;

    @Test
    @DisplayName("When saving a customer it should get an id")
    void whenSavingACustomerItGetsAnId() {
        CustomerJpaEntity customerJpaEntity = getTestCustomer();

        customerRepository.save(customerJpaEntity);

        then(customerJpaEntity.getId()).isNotNull();
    }

    @Test
    @DisplayName("When searching a customer by it's name we should have a result")
    void whenSearchByName() {
        final CustomerJpaEntity testCustomerJpaEntity = getTestCustomer();
        customerRepository.saveAndFlush(testCustomerJpaEntity);

        final CustomerJpaEntity result = customerRepository.findFirstByFirstNameContainingIgnoreCase("Peter");

        then(result.getLastName()).isEqualTo(testCustomerJpaEntity.getLastName());
    }

    private CustomerJpaEntity getTestCustomer() {
        return CustomerJpaEntity.builder()
                .firstName("Peter")
                .lastName("Smith")
                .country("Hungary")
                .zip("1165")
                .city("Budapest")
                .streetAddress("Rutafa utca 11")
                .build();
    }
}
