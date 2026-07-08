package dev.ronin.demo.beerstore.product.application.port.out;

import dev.ronin.demo.beerstore.product.domain.model.Beer;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeerRepository {

    List<Beer> findAllById(List<Long> ids);

    List<Beer> findAll();

    Optional<Beer> findById(Long id);

    Beer save(Beer data);
}
