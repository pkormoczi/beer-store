package dev.ronin.demo.beerstore.catalog.persistence;

import dev.ronin.demo.beerstore.catalog.Beer;
import org.mapstruct.Mapper;

@Mapper
public interface BeerPersistenceMapper {

    BeerData toData(Beer beer);

    Beer toDomain(BeerData data);
}
