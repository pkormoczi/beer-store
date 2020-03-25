package dev.ronin.demo.beerstore.infrastructure.adapter;

import dev.ronin.demo.beerstore.contract.customer.CustomerModel;
import dev.ronin.demo.beerstore.domain.customer.model.Customer;
import org.mapstruct.Mapper;

@Mapper
public interface CustomerMapper {

    CustomerModel data(final Customer entity);
}
