package dev.ronin.demo.beerstore.domain.customer.data;

import dev.ronin.demo.beerstore.domain.customer.value.Address;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "CUSTOMER")
public class CustomerData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    @Embedded
    private Address address;
}
