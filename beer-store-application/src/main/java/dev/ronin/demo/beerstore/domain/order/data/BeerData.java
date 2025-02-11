package dev.ronin.demo.beerstore.domain.order.data;

import dev.ronin.demo.beerstore.domain.order.value.BeerStyle;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "BEER")
public class BeerData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private BeerStyle beerStyle;
}
