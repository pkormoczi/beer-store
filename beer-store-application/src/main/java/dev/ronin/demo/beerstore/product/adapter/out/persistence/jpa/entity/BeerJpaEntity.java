package dev.ronin.demo.beerstore.product.adapter.out.persistence.jpa.entity;

import dev.ronin.demo.beerstore.product.api.type.BeerAvailability;
import dev.ronin.demo.beerstore.product.api.type.BeerStyle;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "BEER")
public class BeerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private BeerStyle beerStyle;

    private double abv;

    private BigDecimal priceAmount;

    @Enumerated(EnumType.STRING)
    private BeerAvailability availability;
}
