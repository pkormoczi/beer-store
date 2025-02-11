package dev.ronin.demo.beerstore.domain.customer.value;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Address {

    private String country;
    private String zip;
    private String city;
    private String streetAddress;

}
