package dev.ronin.demo.beerstore.infrastructure.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderData {

    private Long customerId;
    private List<Long> beers;

}
