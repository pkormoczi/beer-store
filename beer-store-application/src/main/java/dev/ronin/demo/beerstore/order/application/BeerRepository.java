package dev.ronin.demo.beerstore.order.application;

import dev.ronin.demo.beerstore.order.Beer;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeerRepository {

    List<Beer> findAllById(List<Long> ids);
}
