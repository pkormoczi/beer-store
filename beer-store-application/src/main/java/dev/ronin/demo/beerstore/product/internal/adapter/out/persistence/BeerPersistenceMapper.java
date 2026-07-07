package dev.ronin.demo.beerstore.product.internal.adapter.out.persistence;

import dev.ronin.demo.beerstore.product.internal.domain.model.Beer;
import dev.ronin.demo.beerstore.shared.kernel.Money;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

@Mapper
public interface BeerPersistenceMapper {

    @Mapping(target = "priceAmount", source = "price.amount")
    BeerJpaEntity toData(Beer beer);

    @Mapping(target = "price", source = "priceAmount", qualifiedByName = "toMoney")
    Beer toDomain(BeerJpaEntity data);

    @org.mapstruct.Named("toMoney")
    default Money toMoney(BigDecimal amount) {
        return amount == null ? null : new Money(amount);
    }
}
