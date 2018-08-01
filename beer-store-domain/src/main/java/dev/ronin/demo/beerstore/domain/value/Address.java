package dev.ronin.demo.beerstore.domain.value;

import lombok.*;

import javax.persistence.Embeddable;

@Embeddable
@Value
public class Address {

    private final String country;
    private final String city;
    private final String streetAddress;
    private final String zip;
}
