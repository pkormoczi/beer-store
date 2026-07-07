package dev.ronin.demo.beerstore.platform.rest;

import dev.ronin.demo.beerstore.platform.security.AuthorizationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles the exceptions common to every module: generic "not found"/"bad request" cases,
 * cross-cutting {@link AuthorizationException} (from {@code platform.security}, an intra-module
 * reference) and Spring's own validation failures. Deliberately does NOT reference any business
 * module's domain exception types (e.g. {@code CustomerNotFoundException}, {@code
 * OrderNotFoundException}) - the platform module must not depend on business modules, so each
 * module maps its own domain exceptions to HTTP responses in its own {@code
 * internal.adapter.in.rest} package (see {@code CustomerRestExceptionHandler}/{@code
 * OrderRestExceptionHandler}).
 */
@RestControllerAdvice
public class CommonRestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorDetails> notFoundException(final RuntimeException e) {
        return error(e, HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> invalidRequestException(final RuntimeException e) {
        return error(e, HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorDetails> authorizationException(final AuthorizationException e) {
        return error(e, HttpStatus.FORBIDDEN, "Authorization failed!");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                    HttpHeaders headers, HttpStatusCode status,
                                                                    WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> "%s: %s".formatted(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>(new ErrorDetails(message, "Validation failed"), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ErrorDetails> error(final Exception exception, final HttpStatus httpStatus, final String logRef) {
        final String message = Optional.ofNullable(exception.getMessage()).orElse(exception.getClass().getSimpleName());
        ErrorDetails errorDetails = new ErrorDetails(message, logRef);
        return new ResponseEntity<>(errorDetails, httpStatus);
    }
}
