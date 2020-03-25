package dev.ronin.demo.beerstore.domain.order.model;

import dev.ronin.demo.beerstore.domain.customer.model.Customer;
import dev.ronin.demo.beerstore.domain.order.value.OrderStatus;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "BEER_ORDER")
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.NEW;

    @ManyToOne(cascade = CascadeType.ALL)
    private Customer customer;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Singular
    private List<Beer> beers;




}
