package dev.ronin.demo.beerstore.product.adapter.in.rest;

import dev.ronin.demo.beerstore.base.IntegrationTest;
import dev.ronin.demo.beerstore.product.api.BeerManagement;
import dev.ronin.demo.beerstore.product.api.command.CreateBeer;
import dev.ronin.demo.beerstore.product.api.exception.BeerNotFoundException;
import dev.ronin.demo.beerstore.product.api.type.BeerStyle;
import dev.ronin.demo.beerstore.product.api.view.BeerView;
import dev.ronin.demo.beerstore.shared.api.model.BeerDto;
import dev.ronin.demo.beerstore.shared.kernel.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CatalogIT extends IntegrationTest {

    @Autowired
    CatalogController catalogController;

    @Autowired
    BeerManagement beerManagement;

    @Test
    @DisplayName("Browse returns every beer in the catalog")
    void browseReturnsEveryBeer() {
        //Given
        BeerView first = beerManagement.createBeer(
                new CreateBeer("Csoda IPA", BeerStyle.IPA, 5.5, new Money(new BigDecimal("2.50"))));
        BeerView second = beerManagement.createBeer(
                new CreateBeer("Bakony Lager", BeerStyle.LAGER, 4.8, new Money(new BigDecimal("1.90"))));
        //When
        ResponseEntity<List<BeerDto>> result = catalogController.browseBeers(
                null, null, null, null, null, null, null, null);
        //Then
        assertThat(result.getBody())
                .extracting(BeerDto::getId)
                .contains(first.id(), second.id());
    }

    @Test
    @DisplayName("Fetching a beer by id returns its full details")
    void getBeerByIdReturnsBeer() {
        //Given
        BeerView saved = beerManagement.createBeer(
                new CreateBeer("Csoda IPA", BeerStyle.IPA, 5.5, new Money(new BigDecimal("2.50"))));
        //When
        ResponseEntity<BeerDto> result = catalogController.getBeerById(saved.id());
        //Then
        BeerDto body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getName()).isEqualTo("Csoda IPA");
        assertThat(body.getAbv()).isEqualTo(5.5);
        assertThat(body.getPrice()).isEqualByComparingTo("2.50");
    }

    @Test
    @DisplayName("Fetching a non-existent beer throws")
    void getBeerByIdThrowsWhenMissing() {
        assertThatThrownBy(() -> catalogController.getBeerById(999_999L))
                .isInstanceOf(BeerNotFoundException.class);
    }
}
