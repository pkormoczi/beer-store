package dev.ronin.demo.beerstore.catalog.application;

import dev.ronin.demo.beerstore.catalog.Beer;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeerRepository {

    List<Beer> findAllById(List<Long> ids);

    Beer save(Beer data);
}
