package dev.ronin.demo.beerstore.catalog.application;

import dev.ronin.demo.beerstore.catalog.Beer;
import dev.ronin.demo.beerstore.catalog.BeerStyle;
import dev.ronin.demo.beerstore.catalog.ManageBeersUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class Beers implements ManageBeersUseCase {

    private final BeerRepository beerRepository;

    public Beers(BeerRepository beerRepository) {
        this.beerRepository = beerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Beer> findAllById(List<Long> ids) {
        return beerRepository.findAllById(ids);
    }

    @Override
    @Transactional
    public Beer createBeer(String name, BeerStyle beerStyle) {
        return beerRepository.save(new Beer(null, name, beerStyle));
    }
}
