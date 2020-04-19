package dev.ronin.demo.beerstore.adapter.mapper;

import dev.ronin.demo.beerstore.contract.customer.CustomerModel;
import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import org.mapstruct.Mapper;

@Mapper
public interface CustomerMapper {

    CustomerModel data(final CustomerData entity);
}
