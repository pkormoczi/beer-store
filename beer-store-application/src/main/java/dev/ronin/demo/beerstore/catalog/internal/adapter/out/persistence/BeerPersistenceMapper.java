package dev.ronin.demo.beerstore.catalog.internal.adapter.out.persistence;

import dev.ronin.demo.beerstore.catalog.internal.domain.model.Beer;
import org.mapstruct.Mapper;

@Mapper
public interface BeerPersistenceMapper {

    BeerJpaEntity toData(Beer beer);

    Beer toDomain(BeerJpaEntity data);
}
