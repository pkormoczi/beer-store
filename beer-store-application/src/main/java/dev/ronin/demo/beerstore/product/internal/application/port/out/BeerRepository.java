package dev.ronin.demo.beerstore.product.internal.application.port.out;

import dev.ronin.demo.beerstore.product.internal.domain.model.Beer;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeerRepository {

    List<Beer> findAllById(List<Long> ids);

    Beer save(Beer data);
}
