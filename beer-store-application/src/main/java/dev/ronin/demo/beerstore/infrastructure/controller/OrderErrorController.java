package dev.ronin.demo.beerstore.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Optional;

@RestControllerAdvice
public class OrderErrorController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorDetails> notFoundException(final OrderNotFoundException e) {
        return error(e, HttpStatus.NOT_FOUND, e.getMessage());
    }

    private ResponseEntity<ErrorDetails> error(final Exception exception, final HttpStatus httpStatus, final String logRef) {
        final String message = Optional.of(exception.getMessage()).orElse(exception.getClass().getSimpleName());
        ErrorDetails errorDetails = new ErrorDetails(message, logRef);
        return new ResponseEntity<>(errorDetails, httpStatus);
    }
}