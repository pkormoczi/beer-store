package dev.ronin.demo.beerstore.domain;

import dev.ronin.demo.beerstore.domain.value.Address;
import lombok.*;
import org.springframework.beans.factory.annotation.Required;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Customer {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String firstName;

    @NonNull
    private String lastName;

    @Embedded
    @NonNull
    private Address address;
}
