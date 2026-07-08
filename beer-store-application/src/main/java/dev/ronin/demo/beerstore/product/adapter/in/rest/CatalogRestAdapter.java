package dev.ronin.demo.beerstore.product.adapter.in.rest;

import dev.ronin.demo.beerstore.product.api.BeerManagement;
import dev.ronin.demo.beerstore.product.api.query.GetBeer;
import dev.ronin.demo.beerstore.shared.api.model.BeerDto;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bridges the generated {@link dev.ronin.demo.beerstore.shared.api.CatalogApi} wire contract to
 * {@link BeerManagement}. The contract already declares filter/sort query parameters
 * (name/style/minAbv/maxAbv/minPrice/maxPrice/sortBy/sortDirection) for a future round - this
 * first, minimal-skeleton round only lists every beer / fetches one by id, and ignores them.
 */
@Service
public class CatalogRestAdapter {

    private final BeerManagement beerManagement;
    private final CatalogMapper catalogMapper;

    public CatalogRestAdapter(BeerManagement beerManagement, CatalogMapper catalogMapper) {
        this.beerManagement = beerManagement;
        this.catalogMapper = catalogMapper;
    }

    public List<BeerDto> browse() {
        return catalogMapper.toDtoList(beerManagement.listBeers());
    }

    public BeerDto beerWithId(Long id) {
        return catalogMapper.toDto(beerManagement.getBeer(new GetBeer(id)));
    }
}
