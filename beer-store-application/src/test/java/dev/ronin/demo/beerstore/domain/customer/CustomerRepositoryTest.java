package dev.ronin.demo.beerstore.domain.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.BDDAssertions.then;

@DataJpaTest
class CustomerRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void whenSavingACustomerItGetsAnId() {
        Customer customer = getTestCustomer();

        customerRepository.save(customer);

        then(customer.getId()).isNotNull();
    }

    @Test
    void whenSearchByName() {
        final Customer testCustomer = getTestCustomer();
        entityManager.persist(testCustomer);

        final Customer result = customerRepository.findByFirstNameContainingIgnoreCase("Peter");

        then(result.getLastName()).isEqualTo(testCustomer.getLastName());
    }

    private Customer getTestCustomer() {
        return Customer.builder()
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