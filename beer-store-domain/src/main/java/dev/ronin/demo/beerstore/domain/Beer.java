package dev.ronin.demo.beerstore.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Beer {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String name;
}
