package dev.ronin.demo.beerstore.catalog.internal.application.service;

import dev.ronin.demo.beerstore.catalog.api.BeerView;
import dev.ronin.demo.beerstore.catalog.api.CreateBeerCommand;
import dev.ronin.demo.beerstore.catalog.api.FindBeersQuery;
import dev.ronin.demo.beerstore.catalog.api.ManageBeersUseCase;
import dev.ronin.demo.beerstore.catalog.internal.application.port.out.BeerRepository;
import dev.ronin.demo.beerstore.catalog.internal.domain.model.Beer;
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
    public List<BeerView> findAllById(FindBeersQuery query) {
        return beerRepository.findAllById(query.ids()).stream().map(Beers::toView).toList();
    }

    @Override
    @Transactional
    public BeerView createBeer(CreateBeerCommand command) {
        Beer saved = beerRepository.save(Beer.create(command.name(), command.beerStyle(), command.price()));
        return toView(saved);
    }

    private static BeerView toView(Beer beer) {
        return new BeerView(beer.id(), beer.name(), beer.beerStyle(), beer.price());
    }
}
