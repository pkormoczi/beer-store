package dev.ronin.demo.beerstore.shared.kernel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;

class MoneyTest {

    @Test
    @DisplayName("Negative amounts are rejected")
    void negativeAmountIsRejected() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Null amount is rejected")
    void nullAmountIsRejected() {
        assertThatThrownBy(() -> new Money(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Adding two amounts sums them")
    void addSumsAmounts() {
        Money result = new Money(new BigDecimal("2.50")).add(new Money(new BigDecimal("1.25")));

        then(result.amount()).isEqualByComparingTo("3.75");
    }

    @Test
    @DisplayName("Multiplying by a quantity scales the amount")
    void multiplyScalesAmount() {
        Money result = new Money(new BigDecimal("2.50")).multiply(3);

        then(result.amount()).isEqualByComparingTo("7.50");
    }
}
