package dev.ronin.demo.beerstore.order.adapter.out.product;

import dev.ronin.demo.beerstore.product.api.BeerManagement;
import dev.ronin.demo.beerstore.product.api.query.FindBeers;
import dev.ronin.demo.beerstore.product.api.view.BeerView;
import dev.ronin.demo.beerstore.order.application.port.out.BeerLookup;
import dev.ronin.demo.beerstore.order.application.port.out.BeerSnapshot;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class BeerLookupAdapter implements BeerLookup {

    private final BeerManagement beerManagement;

    BeerLookupAdapter(BeerManagement beerManagement) {
        this.beerManagement = beerManagement;
    }

    @Override
    public List<BeerSnapshot> findExisting(List<Long> beerIds) {
        return beerManagement.findAllById(new FindBeers(beerIds)).stream()
                .map(BeerLookupAdapter::toSnapshot)
                .toList();
    }

    private static BeerSnapshot toSnapshot(BeerView beerView) {
        return new BeerSnapshot(beerView.id(), beerView.name(), beerView.price());
    }
}
