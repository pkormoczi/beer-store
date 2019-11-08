package dev.ronin.demo.beerstore.domain.order;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class OrderRepositoryIT {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void testIfDataSqlWorks() {
        Assertions.assertThat(orderRepository.findById(1L).get().getBeers().size()).isEqualTo(2L);
    }


}
