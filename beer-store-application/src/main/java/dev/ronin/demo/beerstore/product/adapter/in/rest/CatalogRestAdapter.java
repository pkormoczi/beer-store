package dev.ronin.demo.beerstore.product.adapter.in.rest;

import dev.ronin.demo.beerstore.product.api.BeerManagement;
import dev.ronin.demo.beerstore.product.api.query.BrowseCatalog;
import dev.ronin.demo.beerstore.product.api.query.GetBeer;
import dev.ronin.demo.beerstore.shared.api.model.BeerAvailabilityDto;
import dev.ronin.demo.beerstore.shared.api.model.BeerDto;
import dev.ronin.demo.beerstore.shared.api.model.BeerSortFieldDto;
import dev.ronin.demo.beerstore.shared.api.model.BeerStyleDto;
import dev.ronin.demo.beerstore.shared.api.model.SortDirectionDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Bridges the generated {@link dev.ronin.demo.beerstore.shared.api.CatalogApi} wire contract to
 * {@link BeerManagement}: translates the wire-format filter/sort DTOs to domain enums via
 * {@link CatalogMapper} and assembles a {@link BrowseCatalog} query.
 */
@Service
public class CatalogRestAdapter {

    private final BeerManagement beerManagement;
    private final CatalogMapper catalogMapper;

    public CatalogRestAdapter(BeerManagement beerManagement, CatalogMapper catalogMapper) {
        this.beerManagement = beerManagement;
        this.catalogMapper = catalogMapper;
    }

    public List<BeerDto> browse(String name, BeerStyleDto style, Double minAbv, Double maxAbv, BigDecimal minPrice,
            BigDecimal maxPrice, List<BeerAvailabilityDto> availability, BeerSortFieldDto sortBy, SortDirectionDto sortDirection) {
        BrowseCatalog query = new BrowseCatalog(name, catalogMapper.toBeerStyle(style), minAbv, maxAbv, minPrice,
                maxPrice, catalogMapper.toAvailabilities(availability), catalogMapper.toSortField(sortBy),
                catalogMapper.toSortDirection(sortDirection));
        return catalogMapper.toDtoList(beerManagement.browse(query));
    }

    public BeerDto beerWithId(Long id) {
        return catalogMapper.toDto(beerManagement.getBeer(new GetBeer(id)));
    }
}
