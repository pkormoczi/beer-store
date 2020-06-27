package dev.ronin.demo.beerstore.infrastructure.adapter.mapper;

import dev.ronin.demo.beerstore.contract.customerdata.CustomerModel;
import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import org.mapstruct.Mapper;

@Mapper
public interface CustomerMapper {

    CustomerModel data(final CustomerData entity);
}
