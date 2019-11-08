package dev.ronin.demo.beerstore.domain.order;

import dev.ronin.demo.beerstore.domain.customer.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private List<Beer> beers;




}
