package dev.ronin.demo.beerstore.order.internal.adapter.out.catalog;

import dev.ronin.demo.beerstore.catalog.api.BeerView;
import dev.ronin.demo.beerstore.catalog.api.FindBeersQuery;
import dev.ronin.demo.beerstore.catalog.api.ManageBeersUseCase;
import dev.ronin.demo.beerstore.order.internal.application.port.out.BeerLookup;
import dev.ronin.demo.beerstore.order.internal.application.port.out.BeerSnapshot;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class BeerLookupAdapter implements BeerLookup {

    private final ManageBeersUseCase manageBeersUseCase;

    BeerLookupAdapter(ManageBeersUseCase manageBeersUseCase) {
        this.manageBeersUseCase = manageBeersUseCase;
    }

    @Override
    public List<BeerSnapshot> findExisting(List<Long> beerIds) {
        return manageBeersUseCase.findAllById(new FindBeersQuery(beerIds)).stream()
                .map(BeerLookupAdapter::toSnapshot)
                .toList();
    }

    private static BeerSnapshot toSnapshot(BeerView beerView) {
        return new BeerSnapshot(beerView.id(), beerView.name(), beerView.price());
    }
}
