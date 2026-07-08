package dev.ronin.demo.beerstore.customer.internal.adapter.out.persistence;

import dev.ronin.demo.beerstore.customer.api.type.CustomerStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "CUSTOMER")
public class CustomerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String country;

    private String zip;

    private String city;

    private String streetAddress;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.ACTIVE;
}
