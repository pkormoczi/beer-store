package dev.ronin.demo.beerstore.domain.order.repository;

import dev.ronin.demo.beerstore.domain.order.data.BeerData;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeerRepository {

    List<BeerData> findAllById(List<Long> ids);
}
