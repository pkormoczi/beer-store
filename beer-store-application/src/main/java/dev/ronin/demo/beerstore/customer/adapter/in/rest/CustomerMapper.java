package dev.ronin.demo.beerstore.customer.adapter.in.rest;

import dev.ronin.demo.beerstore.customer.api.type.Address;
import dev.ronin.demo.beerstore.customer.api.view.CustomerView;
import dev.ronin.demo.beerstore.shared.api.model.AddressModel;
import dev.ronin.demo.beerstore.shared.api.model.CustomerModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface CustomerMapper {

    /**
     * {@link Address} is an immutable value object without setters, so it's built explicitly
     * here rather than relying on MapStruct's reflective constructor-parameter matching.
     */
    default Address toAddress(AddressModel addressModel) {
        if (addressModel == null) {
            return null;
        }
        return new Address(addressModel.getCountry(), addressModel.getZip(),
                addressModel.getCity(), addressModel.getStreetAddress());
    }

    CustomerModel toModel(final CustomerView customer);

    /**
     * Used by contract-test fixtures to build a {@link CustomerView} straight from a
     * {@code CustomerModel} JSON fixture, going through the production mapper.
     */
    @Mapping(target = "status", ignore = true) // not present on the wire CustomerModel
    CustomerView toView(final CustomerModel model);

    dev.ronin.demo.beerstore.shared.contract.customerdata.Customer toWsModel(final CustomerView customer);

    List<CustomerModel> data(final List<CustomerView> customers);
}
