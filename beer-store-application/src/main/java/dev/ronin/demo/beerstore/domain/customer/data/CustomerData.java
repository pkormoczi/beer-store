package dev.ronin.demo.beerstore.domain.customer.data;

import dev.ronin.demo.beerstore.domain.customer.value.Address;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "CUSTOMER")
public class CustomerData {

    public CustomerData(String firstName, String lastName, Address address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    @Embedded
    private Address address;
}
