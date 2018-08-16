package dev.ronin.demo.beerstore.domain.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    private String country;
    private String zip;
    private String city;
    private String streetAddress;

}