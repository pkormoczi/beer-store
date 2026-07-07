package dev.ronin.demo.beerstore.customer.internal.adapter.out.persistence;

import dev.ronin.demo.beerstore.customer.api.Address;
import dev.ronin.demo.beerstore.customer.internal.domain.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CustomerPersistenceMapper {

    @Mapping(target = "country", source = "address.country")
    @Mapping(target = "zip", source = "address.zip")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "streetAddress", source = "address.streetAddress")
    CustomerJpaEntity toData(Customer customer);

    default Customer toDomain(CustomerJpaEntity data) {
        if (data == null) {
            return null;
        }
        Address address = new Address(data.getCountry(), data.getZip(), data.getCity(), data.getStreetAddress());
        return new Customer(data.getId(), data.getFirstName(), data.getLastName(), address, data.getStatus());
    }
}
