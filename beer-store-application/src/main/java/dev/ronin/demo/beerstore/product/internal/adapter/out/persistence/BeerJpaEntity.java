package dev.ronin.demo.beerstore.product.internal.adapter.out.persistence;

import dev.ronin.demo.beerstore.product.api.BeerStyle;
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

    private BigDecimal priceAmount;
}
