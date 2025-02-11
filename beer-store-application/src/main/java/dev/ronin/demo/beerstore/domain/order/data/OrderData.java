package dev.ronin.demo.beerstore.domain.order.data;

import dev.ronin.demo.beerstore.domain.customer.data.CustomerData;
import dev.ronin.demo.beerstore.domain.order.value.OrderStatus;
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

    @ManyToOne(cascade = CascadeType.ALL)
    private CustomerData customer;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(joinColumns = @JoinColumn(name = "order_id"), inverseJoinColumns = @JoinColumn(name = "beer_id"))
    @Singular
    private List<BeerData> beers;

}
