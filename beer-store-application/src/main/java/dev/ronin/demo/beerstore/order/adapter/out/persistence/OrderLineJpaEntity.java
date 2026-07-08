package dev.ronin.demo.beerstore.order.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "BEER_ORDER_LINE")
public class OrderLineJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderJpaEntity order;

    private Long beerId;

    private String beerNameSnapshot;

    private BigDecimal unitPriceSnapshotAmount;

    private int quantity;
}
