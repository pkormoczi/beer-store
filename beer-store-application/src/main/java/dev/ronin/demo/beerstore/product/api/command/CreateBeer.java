package dev.ronin.demo.beerstore.product.api.command;

import dev.ronin.demo.beerstore.product.api.type.BeerAvailability;
import dev.ronin.demo.beerstore.product.api.type.BeerStyle;
import dev.ronin.demo.beerstore.shared.kernel.Money;

public record CreateBeer(String name, BeerStyle beerStyle, double abv, Money price, BeerAvailability availability) {

    /**
     * Convenience overload for call sites that don't care about the beer's initial availability -
     * defaults to {@link BeerAvailability#IN_STOCK}, mirroring
     * {@link dev.ronin.demo.beerstore.product.domain.model.Beer#create(String, BeerStyle, double, dev.ronin.demo.beerstore.shared.kernel.Money)}.
     */
    public CreateBeer(String name, BeerStyle beerStyle, double abv, Money price) {
        this(name, beerStyle, abv, price, BeerAvailability.IN_STOCK);
    }
}
