package dev.ronin.demo.beerstore.infrastructure.controller;


import dev.ronin.demo.beerstore.infrastructure.api.HomeApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.String.format;

@RestController
public class HomeController implements HomeApi {

    @Override
    public ResponseEntity<String> hello(String name) {
        return ResponseEntity.ok(format("Hello %s!", name));
    }
}
