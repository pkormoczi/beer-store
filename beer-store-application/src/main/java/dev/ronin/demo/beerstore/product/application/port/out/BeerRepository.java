package dev.ronin.demo.beerstore.product.application.port.out;

import dev.ronin.demo.beerstore.product.domain.model.Beer;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeerRepository {

    List<Beer> findAllById(List<Long> ids);

    Beer save(Beer data);
}
