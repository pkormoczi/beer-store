package dev.ronin.demo.beerstore.adapter;

import dev.ronin.beerstore.contract.customer.Customer;
import org.mapstruct.Mapper;

@Mapper
interface CustomerMapper {

    Customer toDto(final dev.ronin.demo.beerstore.domain.customer.Customer entity);
}
