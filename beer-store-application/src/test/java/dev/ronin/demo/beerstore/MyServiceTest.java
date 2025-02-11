package dev.ronin.demo.beerstore;

import dev.ronin.demo.beerstore.domain.MyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class MyServiceTest {

    @Autowired
    private MyService myService;
    
    @Test
    @DisplayName("asdasdasdas")
    void asdasdasdas() {
        //Given
        
        //When
        
        //Then
        
    }

    @Test
    void testProcessDataWithMockedPort() {

        String result = myService.processData();
        assertThat(result).isEqualTo("Processed Mocked Data");
    }
}