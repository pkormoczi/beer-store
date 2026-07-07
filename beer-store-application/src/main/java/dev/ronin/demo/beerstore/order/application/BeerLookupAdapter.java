package dev.ronin.demo.beerstore.order.application;

import dev.ronin.demo.beerstore.catalog.Beer;
import dev.ronin.demo.beerstore.catalog.ManageBeersUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class BeerLookupAdapter implements BeerLookup {

    private final ManageBeersUseCase manageBeersUseCase;

    BeerLookupAdapter(ManageBeersUseCase manageBeersUseCase) {
        this.manageBeersUseCase = manageBeersUseCase;
    }

    @Override
    public List<Long> findExistingIds(List<Long> ids) {
        return manageBeersUseCase.findAllById(ids).stream().map(Beer::id).toList();
    }
}
