package dev.ronin.demo.beerstore.customer.internal.adapter.in.rest;

import dev.ronin.demo.beerstore.customer.api.CustomerNotFoundException;
import dev.ronin.demo.beerstore.platform.rest.ErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps the customer module's own domain exceptions to HTTP responses. Kept in this module
 * (rather than a global handler) because {@code platform} must not depend on business modules -
 * see {@code platform.rest.CommonRestExceptionHandler}, which still handles the exceptions
 * common to every module (generic not-found, validation, authorization).
 */
@RestControllerAdvice
public class CustomerRestExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorDetails> customerNotFoundException(final CustomerNotFoundException e) {
        return new ResponseEntity<>(new ErrorDetails(e.getMessage(), e.getMessage()), HttpStatus.NOT_FOUND);
    }
}
