package dev.ronin.demo.beerstore;

import dev.ronin.demo.beerstore.base.IntegrationTest;
import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import dev.ronin.demo.beerstore.domain.customer.repository.CustomerRepository;
import dev.ronin.demo.beerstore.domain.customer.value.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.BDDAssertions.then;

//@DataJpaTest
@Transactional
@AutoConfigureCache
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
class CustomerDataRepositoryIT extends IntegrationTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

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
        entityManager.persist(testCustomerData);

        final CustomerData result = customerRepository.findFirstByFirstNameContainingIgnoreCase("Peter");

        then(result.getLastName()).isEqualTo(testCustomerData.getLastName());
    }

    private CustomerData getTestCustomer() {
        return CustomerData.builder()
                .firstName("Peter")
                .lastName("Smith")
                .address(Address.builder()
                        .country("Hungary")
                        .city("Budapest")
                        .zip("1165")
                        .streetAddress("Rutafa utca 11").build())
                .build();
    }
}