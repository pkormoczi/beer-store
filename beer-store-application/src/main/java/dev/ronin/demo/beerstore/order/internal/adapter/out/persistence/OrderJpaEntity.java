package dev.ronin.demo.beerstore.order.internal.adapter.out.persistence;

import dev.ronin.demo.beerstore.order.api.type.OrderStatus;
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
public class OrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.NEW;

    private Long customerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Singular
    private List<OrderLineJpaEntity> lines;

}
