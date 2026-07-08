package dev.ronin.demo.beerstore.product.application.service;

import dev.ronin.demo.beerstore.product.api.BeerManagement;
import dev.ronin.demo.beerstore.product.api.command.CreateBeer;
import dev.ronin.demo.beerstore.product.api.query.FindBeers;
import dev.ronin.demo.beerstore.product.api.view.BeerView;
import dev.ronin.demo.beerstore.product.application.port.out.BeerRepository;
import dev.ronin.demo.beerstore.product.domain.model.Beer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class Beers implements BeerManagement {

    private final BeerRepository beerRepository;

    public Beers(BeerRepository beerRepository) {
        this.beerRepository = beerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeerView> findAllById(FindBeers query) {
        return beerRepository.findAllById(query.ids()).stream().map(Beers::toView).toList();
    }

    @Override
    @Transactional
    public BeerView createBeer(CreateBeer command) {
        Beer saved = beerRepository.save(Beer.create(command.name(), command.beerStyle(), command.price()));
        return toView(saved);
    }

    private static BeerView toView(Beer beer) {
        return new BeerView(beer.id(), beer.name(), beer.beerStyle(), beer.price());
    }
}
