package dev.ronin.demo.beerstore.infrastructure.adapter.mapper;

import dev.ronin.demo.beerstore.contract.customerdata.Customer;
import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import dev.ronin.demo.beerstore.domain.customer.value.Address;
import dev.ronin.demo.beerstore.infrastructure.api.model.AddressModel;
import dev.ronin.demo.beerstore.infrastructure.api.model.CustomerModel;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface CustomerMapper {

    Address toAddress(AddressModel addressModel);

    CustomerModel toModel(final CustomerData entity);

    Customer toWsModel(final CustomerData entity);

    List<CustomerModel> data(final List<CustomerData> entities);
}
