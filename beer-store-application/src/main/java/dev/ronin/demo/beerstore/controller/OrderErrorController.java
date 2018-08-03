package dev.ronin.demo.beerstore.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setMessage(message);
        errorDetails.setDetails(logRef);
        return new ResponseEntity<>(errorDetails, httpStatus);
    }
//
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<ErrorDetails> assertionException(final IllegalArgumentException e) {
//        return error(e, HttpStatus.NOT_FOUND, e.getLocalizedMessage());
//    }
}