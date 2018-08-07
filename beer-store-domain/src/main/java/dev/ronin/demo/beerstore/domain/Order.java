package dev.ronin.demo.beerstore.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "BEER_ORDER")
@ApiModel("Order entity")
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @ApiModelProperty("Customer who made the order")
    private Customer customer;

    @OneToMany(cascade = CascadeType.ALL)
    @ApiModelProperty("The ordered beer list")
    private List<Beer> beers;




}
