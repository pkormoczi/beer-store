package dev.ronin.demo.beerstore.customer.persistence;

import dev.ronin.demo.beerstore.customer.Address;
import dev.ronin.demo.beerstore.customer.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CustomerPersistenceMapper {

    @Mapping(target = "country", source = "address.country")
    @Mapping(target = "zip", source = "address.zip")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "streetAddress", source = "address.streetAddress")
    CustomerData toData(Customer customer);

    default Customer toDomain(CustomerData data) {
        if (data == null) {
            return null;
        }
        Address address = new Address(data.getCountry(), data.getZip(), data.getCity(), data.getStreetAddress());
        return new Customer(data.getId(), data.getFirstName(), data.getLastName(), address);
    }
}
