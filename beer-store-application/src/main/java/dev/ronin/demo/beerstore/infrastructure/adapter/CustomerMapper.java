package dev.ronin.demo.beerstore.infrastructure.adapter;

import dev.ronin.demo.beerstore.contract.customer.Customer;
import org.mapstruct.Mapper;

@Mapper
public interface CustomerMapper {

    Customer toDto(final dev.ronin.demo.beerstore.domain.customer.Customer entity);
}