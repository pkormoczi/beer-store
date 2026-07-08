package dev.ronin.demo.beerstore.product.adapter.in.rest;

import dev.ronin.demo.beerstore.product.api.type.BeerAvailability;
import dev.ronin.demo.beerstore.product.api.type.BeerSortField;
import dev.ronin.demo.beerstore.product.api.type.BeerStyle;
import dev.ronin.demo.beerstore.product.api.type.SortDirection;
import dev.ronin.demo.beerstore.product.api.view.BeerView;
import dev.ronin.demo.beerstore.shared.api.model.BeerAvailabilityDto;
import dev.ronin.demo.beerstore.shared.api.model.BeerDto;
import dev.ronin.demo.beerstore.shared.api.model.BeerSortFieldDto;
import dev.ronin.demo.beerstore.shared.api.model.BeerStyleDto;
import dev.ronin.demo.beerstore.shared.api.model.SortDirectionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface CatalogMapper {

    @Mapping(target = "price", source = "price.amount")
    BeerDto toDto(BeerView beerView);

    List<BeerDto> toDtoList(List<BeerView> beerViews);

    BeerStyle toBeerStyle(BeerStyleDto style);

    List<BeerAvailability> toAvailabilities(List<BeerAvailabilityDto> availabilities);

    BeerSortField toSortField(BeerSortFieldDto sortBy);

    SortDirection toSortDirection(SortDirectionDto sortDirection);
}
