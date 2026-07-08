package dev.ronin.demo.beerstore.product.adapter.in.rest;

import dev.ronin.demo.beerstore.product.api.view.BeerView;
import dev.ronin.demo.beerstore.shared.api.model.BeerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface CatalogMapper {

    @Mapping(target = "price", source = "price.amount")
    @Mapping(target = "status", ignore = true) // no domain source yet - see ADR/next round (BeerStatus)
    BeerDto toDto(BeerView beerView);

    List<BeerDto> toDtoList(List<BeerView> beerViews);
}
