package dev.ronin.demo.beerstore.infrastructure.controller.handler;

import dev.ronin.demo.beerstore.infrastructure.security.AuthorizationException;
import dev.ronin.demo.beerstore.infrastructure.data.ErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.NoSuchElementException;
import java.util.Optional;

@RestControllerAdvice
public class ErrorHandlingControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorDetails> notFoundException(final NoSuchElementException e) {
        return error(e, HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorDetails> authorizationException(final AuthorizationException e) {
        return error(e, HttpStatus.FORBIDDEN, "Authorization failed!");
    }

    private ResponseEntity<ErrorDetails> error(final Exception exception, final HttpStatus httpStatus, final String logRef) {
        final String message = Optional.of(exception.getMessage()).orElse(exception.getClass().getSimpleName());
        ErrorDetails errorDetails = new ErrorDetails(message, logRef);
        return new ResponseEntity<>(errorDetails, httpStatus);
    }
}