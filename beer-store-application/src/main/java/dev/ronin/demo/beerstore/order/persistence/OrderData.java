package dev.ronin.demo.beerstore.order.persistence;

import dev.ronin.demo.beerstore.order.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "BEER_ORDER")
@Table(name = "BEER_ORDER")
public class OrderData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.NEW;

    private Long customerId;

    @ElementCollection
    @CollectionTable(name = "beer_order_beers", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "beer_id")
    @Singular
    private List<Long> beers;

}
