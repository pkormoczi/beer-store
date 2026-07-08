package dev.ronin.demo.beerstore.customer.adapter.in.rest;

import dev.ronin.demo.beerstore.customer.api.type.Address;
import dev.ronin.demo.beerstore.customer.api.view.CustomerView;
import dev.ronin.demo.beerstore.shared.api.model.AddressDto;
import dev.ronin.demo.beerstore.shared.api.model.CustomerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface CustomerMapper {

    /**
     * {@link Address} is an immutable value object without setters, so it's built explicitly
     * here rather than relying on MapStruct's reflective constructor-parameter matching.
     */
    default Address toAddress(AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }
        return new Address(addressDto.getCountry(), addressDto.getZip(),
                addressDto.getCity(), addressDto.getStreetAddress());
    }

    CustomerDto toDto(final CustomerView customer);

    /**
     * Used by contract-test fixtures to build a {@link CustomerView} straight from a
     * {@code CustomerDto} JSON fixture, going through the production mapper.
     */
    @Mapping(target = "status", ignore = true) // not present on the wire CustomerDto
    CustomerView toView(final CustomerDto dto);

    dev.ronin.demo.beerstore.shared.contract.customerdata.Customer toWsModel(final CustomerView customer);

    List<CustomerDto> data(final List<CustomerView> customers);
}
