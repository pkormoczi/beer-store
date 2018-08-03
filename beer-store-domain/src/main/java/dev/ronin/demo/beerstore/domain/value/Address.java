package dev.ronin.demo.beerstore.domain.value;

import lombok.*;

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
