package dev.ronin.demo.beerstore.customer.web;

import dev.ronin.demo.beerstore.customer.Address;
import dev.ronin.demo.beerstore.customer.Customer;
import dev.ronin.demo.beerstore.shared.api.model.AddressModel;
import dev.ronin.demo.beerstore.shared.api.model.CustomerModel;
import org.mapstruct.Mapper;

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

    CustomerModel toModel(final Customer customer);

    Customer toDomain(final CustomerModel model);

    dev.ronin.demo.beerstore.shared.contract.customerdata.Customer toWsModel(final Customer customer);

    List<CustomerModel> data(final List<Customer> customers);
}
