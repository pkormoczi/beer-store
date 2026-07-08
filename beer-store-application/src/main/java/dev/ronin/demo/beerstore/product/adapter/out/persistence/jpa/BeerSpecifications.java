package dev.ronin.demo.beerstore.product.adapter.out.persistence.jpa;

import dev.ronin.demo.beerstore.product.adapter.out.persistence.jpa.entity.BeerJpaEntity;
import dev.ronin.demo.beerstore.product.api.query.BrowseCatalog;
import dev.ronin.demo.beerstore.product.api.type.BeerSortField;
import dev.ronin.demo.beerstore.product.api.type.SortDirection;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Translates a {@link BrowseCatalog} criteria object - deliberately made up of only this module's
 * own enums/primitives/{@link BigDecimal}, since {@code api}/{@code application}/{@code domain}
 * must not depend on persistence technology - into a JPA {@link Specification}/{@link Sort} pair.
 * A plain static-factory helper: no {@code @Service}/{@code @Repository} and no
 * {@code *Adapter}/{@code *Mapper}/{@code *Repository} suffix, so it doesn't trip any
 * adapter-naming ArchUnit rule; only {@link BeerPersistenceAdapter} (same package) calls it.
 */
final class BeerSpecifications {

    private BeerSpecifications() {
    }

    static Specification<BeerJpaEntity> matching(BrowseCatalog criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.name() != null && !criteria.name().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + criteria.name().toLowerCase() + "%"));
            }
            if (criteria.beerStyle() != null) {
                predicates.add(cb.equal(root.get("beerStyle"), criteria.beerStyle()));
            }
            if (criteria.minAbv() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<Double>get("abv"), criteria.minAbv()));
            }
            if (criteria.maxAbv() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.<Double>get("abv"), criteria.maxAbv()));
            }
            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("priceAmount"), criteria.minPrice()));
            }
            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("priceAmount"), criteria.maxPrice()));
            }
            if (criteria.availabilities() != null && !criteria.availabilities().isEmpty()) {
                predicates.add(root.get("availability").in(criteria.availabilities()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    static Sort sortOf(BeerSortField sortBy, SortDirection sortDirection) {
        String property = switch (sortBy == null ? BeerSortField.NAME : sortBy) {
            case NAME -> "name";
            case STYLE -> "beerStyle";
            case ABV -> "abv";
            case PRICE -> "priceAmount";
            case AVAILABILITY -> "availability";
        };
        Sort.Direction direction = sortDirection == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }
}
