package dev.ronin.demo.beerstore.domain;

import dev.ronin.demo.beerstore.domain.value.BeerStyle;
import lombok.*;

import javax.persistence.*;

@Entity
@Data
@Builder
public class Beer {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private BeerStyle beerStyle;
}
