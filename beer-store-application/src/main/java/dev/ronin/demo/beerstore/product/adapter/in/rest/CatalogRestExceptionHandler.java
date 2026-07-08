package dev.ronin.demo.beerstore.product.adapter.in.rest;

import dev.ronin.demo.beerstore.platform.rest.ErrorDetails;
import dev.ronin.demo.beerstore.product.api.exception.BeerNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps the product module's own domain exceptions to HTTP responses. Kept in this module (rather
 * than a global handler) because {@code platform} must not depend on business modules - see
 * {@code platform.rest.CommonRestExceptionHandler}, which still handles the exceptions common to
 * every module (generic not-found, validation, authorization).
 */
@RestControllerAdvice
public class CatalogRestExceptionHandler {

    @ExceptionHandler(BeerNotFoundException.class)
    public ResponseEntity<ErrorDetails> beerNotFoundException(final BeerNotFoundException e) {
        return new ResponseEntity<>(new ErrorDetails(e.getMessage(), e.getMessage()), HttpStatus.NOT_FOUND);
    }
}
