package dev.ronin.demo.beerstore.product.adapter.in.rest;

import dev.ronin.demo.beerstore.base.IntegrationTest;
import dev.ronin.demo.beerstore.product.api.BeerManagement;
import dev.ronin.demo.beerstore.product.api.command.CreateBeer;
import dev.ronin.demo.beerstore.product.api.exception.BeerNotFoundException;
import dev.ronin.demo.beerstore.product.api.type.BeerAvailability;
import dev.ronin.demo.beerstore.product.api.type.BeerStyle;
import dev.ronin.demo.beerstore.product.api.view.BeerView;
import dev.ronin.demo.beerstore.shared.api.model.BeerAvailabilityDto;
import dev.ronin.demo.beerstore.shared.api.model.BeerDto;
import dev.ronin.demo.beerstore.shared.api.model.BeerSortFieldDto;
import dev.ronin.demo.beerstore.shared.api.model.BeerStyleDto;
import dev.ronin.demo.beerstore.shared.api.model.SortDirectionDto;
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
    @DisplayName("Browse without filters returns every beer, regardless of availability")
    void browseReturnsEveryBeer() {
        //Given
        BeerView inStock = createBeer("Csoda IPA", BeerStyle.IPA, 5.5, "2.50", BeerAvailability.IN_STOCK);
        BeerView discontinued = createBeer("Bakony Lager", BeerStyle.LAGER, 4.8, "1.90", BeerAvailability.DISCONTINUED);
        //When
        List<BeerDto> result = browse(null, null, null, null, null, null, null, null, null);
        //Then
        assertThat(result).extracting(BeerDto::getId).contains(inStock.id(), discontinued.id());
    }

    @Test
    @DisplayName("Browse filters by style")
    void browseFiltersByStyle() {
        //Given
        BeerView ipa = createBeer("Style Filter IPA", BeerStyle.IPA, 5.5, "2.50", BeerAvailability.IN_STOCK);
        BeerView lager = createBeer("Style Filter Lager", BeerStyle.LAGER, 4.8, "1.90", BeerAvailability.IN_STOCK);
        //When
        List<BeerDto> result = browse(null, BeerStyleDto.IPA, null, null, null, null, null, null, null);
        //Then
        assertThat(result).extracting(BeerDto::getId).contains(ipa.id()).doesNotContain(lager.id());
        assertThat(result).extracting(BeerDto::getBeerStyle).allMatch(style -> style == BeerStyleDto.IPA);
    }

    @Test
    @DisplayName("Browse filters by abv range")
    void browseFiltersByAbvRange() {
        //Given
        BeerView low = createBeer("Abv Range Low", BeerStyle.SESSION, 2.0, "1.50", BeerAvailability.IN_STOCK);
        BeerView mid = createBeer("Abv Range Mid", BeerStyle.APA, 5.0, "1.50", BeerAvailability.IN_STOCK);
        BeerView high = createBeer("Abv Range High", BeerStyle.IPA, 9.0, "1.50", BeerAvailability.IN_STOCK);
        //When
        List<BeerDto> result = browse(null, null, 4.0, 6.0, null, null, null, null, null);
        //Then
        assertThat(result).extracting(BeerDto::getId).contains(mid.id()).doesNotContain(low.id(), high.id());
    }

    @Test
    @DisplayName("Browse filters by price range")
    void browseFiltersByPriceRange() {
        //Given
        BeerView cheap = createBeer("Price Range Cheap", BeerStyle.SESSION, 4.0, "0.90", BeerAvailability.IN_STOCK);
        BeerView mid = createBeer("Price Range Mid", BeerStyle.APA, 4.0, "3.00", BeerAvailability.IN_STOCK);
        BeerView expensive = createBeer("Price Range Expensive", BeerStyle.IPA, 4.0, "9.00", BeerAvailability.IN_STOCK);
        //When
        List<BeerDto> result = browse(null, null, null, null, new BigDecimal("2.00"), new BigDecimal("5.00"), null, null, null);
        //Then
        assertThat(result).extracting(BeerDto::getId).contains(mid.id()).doesNotContain(cheap.id(), expensive.id());
    }

    @Test
    @DisplayName("Browse filters by a partial, case-insensitive name match")
    void browseFiltersByName() {
        //Given
        BeerView matching = createBeer("Uniquely Named Special Brew", BeerStyle.ALE, 4.0, "2.00", BeerAvailability.IN_STOCK);
        BeerView other = createBeer("Totally Different Beer", BeerStyle.ALE, 4.0, "2.00", BeerAvailability.IN_STOCK);
        //When
        List<BeerDto> result = browse("special brew", null, null, null, null, null, null, null, null);
        //Then
        assertThat(result).extracting(BeerDto::getId).contains(matching.id()).doesNotContain(other.id());
    }

    @Test
    @DisplayName("Browse filters by multiple availability values")
    void browseFiltersByMultipleAvailabilityValues() {
        //Given
        BeerView inStock = createBeer("Availability Filter In Stock", BeerStyle.ALE, 4.0, "2.00", BeerAvailability.IN_STOCK);
        BeerView lowStock = createBeer("Availability Filter Low Stock", BeerStyle.ALE, 4.0, "2.00", BeerAvailability.LOW_STOCK);
        BeerView discontinued = createBeer("Availability Filter Discontinued", BeerStyle.ALE, 4.0, "2.00", BeerAvailability.DISCONTINUED);
        //When
        List<BeerDto> result = browse(null, null, null, null, null, null,
                List.of(BeerAvailabilityDto.IN_STOCK, BeerAvailabilityDto.LOW_STOCK), null, null);
        //Then
        assertThat(result).extracting(BeerDto::getId)
                .contains(inStock.id(), lowStock.id())
                .doesNotContain(discontinued.id());
    }

    @Test
    @DisplayName("Browse sorts by price descending")
    void browseSortsByPriceDescending() {
        //Given
        BeerView cheap = createBeer("Sort Price Cheap", BeerStyle.SESSION, 4.0, "1.00", BeerAvailability.IN_STOCK);
        BeerView expensive = createBeer("Sort Price Expensive", BeerStyle.IPA, 4.0, "8.00", BeerAvailability.IN_STOCK);
        //When
        List<BeerDto> result = browse(null, null, null, null, null, null, null, BeerSortFieldDto.PRICE, SortDirectionDto.DESC);
        //Then
        List<Long> ids = result.stream().map(BeerDto::getId).toList();
        assertThat(ids.indexOf(expensive.id())).isLessThan(ids.indexOf(cheap.id()));
    }

    @Test
    @DisplayName("Browse sorts by name ascending")
    void browseSortsByNameAscending() {
        //Given
        BeerView first = createBeer("AAA Sort Name Beer", BeerStyle.SESSION, 4.0, "1.00", BeerAvailability.IN_STOCK);
        BeerView last = createBeer("ZZZ Sort Name Beer", BeerStyle.SESSION, 4.0, "1.00", BeerAvailability.IN_STOCK);
        //When
        List<BeerDto> result = browse(null, null, null, null, null, null, null, BeerSortFieldDto.NAME, SortDirectionDto.ASC);
        //Then
        List<Long> ids = result.stream().map(BeerDto::getId).toList();
        assertThat(ids.indexOf(first.id())).isLessThan(ids.indexOf(last.id()));
    }

    @Test
    @DisplayName("Fetching a beer by id returns its full details")
    void getBeerByIdReturnsBeer() {
        //Given
        BeerView saved = createBeer("Csoda IPA", BeerStyle.IPA, 5.5, "2.50", BeerAvailability.IN_STOCK);
        //When
        ResponseEntity<BeerDto> result = catalogController.getBeerById(saved.id());
        //Then
        BeerDto body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getName()).isEqualTo("Csoda IPA");
        assertThat(body.getAbv()).isEqualTo(5.5);
        assertThat(body.getPrice()).isEqualByComparingTo("2.50");
        assertThat(body.getAvailability()).isEqualTo(BeerAvailabilityDto.IN_STOCK);
    }

    @Test
    @DisplayName("Fetching a beer by id returns it regardless of its availability")
    void getBeerByIdReturnsBeerRegardlessOfAvailability() {
        //Given
        BeerView discontinued = createBeer("Discontinued Fetch Beer", BeerStyle.LAGER, 4.0, "1.00", BeerAvailability.DISCONTINUED);
        //When
        ResponseEntity<BeerDto> result = catalogController.getBeerById(discontinued.id());
        //Then
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getAvailability()).isEqualTo(BeerAvailabilityDto.DISCONTINUED);
    }

    @Test
    @DisplayName("Fetching a non-existent beer throws")
    void getBeerByIdThrowsWhenMissing() {
        assertThatThrownBy(() -> catalogController.getBeerById(999_999L))
                .isInstanceOf(BeerNotFoundException.class);
    }

    private BeerView createBeer(String name, BeerStyle style, double abv, String price, BeerAvailability availability) {
        return beerManagement.createBeer(new CreateBeer(name, style, abv, new Money(new BigDecimal(price)), availability));
    }

    private List<BeerDto> browse(String name, BeerStyleDto style, Double minAbv, Double maxAbv, BigDecimal minPrice,
            BigDecimal maxPrice, List<BeerAvailabilityDto> availability, BeerSortFieldDto sortBy, SortDirectionDto sortDirection) {
        return catalogController.browseBeers(name, style, minAbv, maxAbv, minPrice, maxPrice, availability, sortBy, sortDirection)
                .getBody();
    }
}
