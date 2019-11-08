package dev.ronin.demo.beerstore.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
class OrderNotFoundException extends RuntimeException {
    OrderNotFoundException(String message) {
        super(message);
    }
}
