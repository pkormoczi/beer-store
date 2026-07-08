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
    private static final double ABV = 5.5;

    @Test
    @DisplayName("A newly created beer has no id and the given attributes")
    void createBuildsUnsavedBeer() {
        Beer beer = Beer.create("Csoda IPA", BeerStyle.IPA, ABV, PRICE);

        then(beer.id()).isNull();
        then(beer.name()).isEqualTo("Csoda IPA");
        then(beer.beerStyle()).isEqualTo(BeerStyle.IPA);
        then(beer.abv()).isEqualTo(ABV);
        then(beer.price()).isEqualTo(PRICE);
    }

    @Test
    @DisplayName("A blank name is rejected")
    void blankNameIsRejected() {
        assertThatThrownBy(() -> Beer.create("  ", BeerStyle.IPA, ABV, PRICE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A null beer style is rejected")
    void nullBeerStyleIsRejected() {
        assertThatThrownBy(() -> Beer.create("Csoda IPA", null, ABV, PRICE))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("A negative abv is rejected")
    void negativeAbvIsRejected() {
        assertThatThrownBy(() -> Beer.create("Csoda IPA", BeerStyle.IPA, -0.1, PRICE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("An abv above 100 is rejected")
    void abvAboveMaxIsRejected() {
        assertThatThrownBy(() -> Beer.create("Csoda IPA", BeerStyle.IPA, 100.1, PRICE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A null price is rejected")
    void nullPriceIsRejected() {
        assertThatThrownBy(() -> Beer.create("Csoda IPA", BeerStyle.IPA, ABV, null))
                .isInstanceOf(NullPointerException.class);
    }
}
