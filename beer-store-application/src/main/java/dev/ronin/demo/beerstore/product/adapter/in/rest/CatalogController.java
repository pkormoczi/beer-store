package dev.ronin.demo.beerstore.product.adapter.in.rest;

import dev.ronin.demo.beerstore.shared.api.CatalogApi;
import dev.ronin.demo.beerstore.shared.api.model.BeerDto;
import dev.ronin.demo.beerstore.shared.api.model.BeerSortFieldDto;
import dev.ronin.demo.beerstore.shared.api.model.BeerStyleDto;
import dev.ronin.demo.beerstore.shared.api.model.SortDirectionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class CatalogController implements CatalogApi {

    private final CatalogRestAdapter catalogRestAdapter;

    public CatalogController(CatalogRestAdapter catalogRestAdapter) {
        this.catalogRestAdapter = catalogRestAdapter;
    }

    @Override
    public ResponseEntity<List<BeerDto>> browseBeers(String name, BeerStyleDto style, Double minAbv, Double maxAbv,
            BigDecimal minPrice, BigDecimal maxPrice, BeerSortFieldDto sortBy, SortDirectionDto sortDirection) {
        return ResponseEntity.ok(catalogRestAdapter.browse());
    }

    @Override
    public ResponseEntity<BeerDto> getBeerById(Long id) {
        return ResponseEntity.ok(catalogRestAdapter.beerWithId(id));
    }
}
