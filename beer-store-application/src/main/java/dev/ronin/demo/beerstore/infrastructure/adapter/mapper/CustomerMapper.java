package dev.ronin.demo.beerstore.infrastructure.adapter.mapper;

import dev.ronin.demo.beerstore.contract.customerdata.CustomerModel;
import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface CustomerMapper {

    CustomerModel data(final CustomerData entity);

    List<CustomerModel> data(final List<CustomerData> entities);
}
