package dev.ronin.demo.beerstore.product.domain.model;

import dev.ronin.demo.beerstore.product.api.type.BeerStyle;
import dev.ronin.demo.beerstore.shared.kernel.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;

class BeerTest {

    private static final Money PRICE = new Money(new BigDecimal("2.50"));

    @Test
    @DisplayName("A newly created beer has no id and the given attributes")
    void createBuildsUnsavedBeer() {
        Beer beer = Beer.create("Csoda IPA", BeerStyle.IPA, PRICE);

        then(beer.id()).isNull();
        then(beer.name()).isEqualTo("Csoda IPA");
        then(beer.beerStyle()).isEqualTo(BeerStyle.IPA);
        then(beer.price()).isEqualTo(PRICE);
    }

    @Test
    @DisplayName("A blank name is rejected")
    void blankNameIsRejected() {
        assertThatThrownBy(() -> Beer.create("  ", BeerStyle.IPA, PRICE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A null beer style is rejected")
    void nullBeerStyleIsRejected() {
        assertThatThrownBy(() -> Beer.create("Csoda IPA", null, PRICE))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("A null price is rejected")
    void nullPriceIsRejected() {
        assertThatThrownBy(() -> Beer.create("Csoda IPA", BeerStyle.IPA, null))
                .isInstanceOf(NullPointerException.class);
    }
}
