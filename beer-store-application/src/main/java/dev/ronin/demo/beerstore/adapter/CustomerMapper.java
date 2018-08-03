package dev.ronin.demo.beerstore.adapter;

import dev.ronin.beerstore.contract.customer.Customer;
import org.mapstruct.Mapper;

@Mapper
public interface CustomerMapper {

    Customer toDto(final dev.ronin.demo.beerstore.domain.Customer entity);
}
