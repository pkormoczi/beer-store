package dev.ronin.demo.beerstore.order.internal.adapter.in.rest;

import dev.ronin.demo.beerstore.order.api.exception.IllegalOrderStateException;
import dev.ronin.demo.beerstore.order.api.exception.OrderNotFoundException;
import dev.ronin.demo.beerstore.order.api.exception.UnknownBeerException;
import dev.ronin.demo.beerstore.platform.rest.ErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps the order module's own domain exceptions to HTTP responses. Kept in this module (rather
 * than a global handler) because {@code platform} must not depend on business modules - see
 * {@code platform.rest.CommonRestExceptionHandler}, which still handles the exceptions common to
 * every module (generic not-found, validation, authorization).
 */
@RestControllerAdvice
public class OrderRestExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorDetails> orderNotFoundException(final OrderNotFoundException e) {
        return new ResponseEntity<>(new ErrorDetails(e.getMessage(), e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({IllegalOrderStateException.class, UnknownBeerException.class})
    public ResponseEntity<ErrorDetails> invalidOrderRequestException(final RuntimeException e) {
        return new ResponseEntity<>(new ErrorDetails(e.getMessage(), e.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
