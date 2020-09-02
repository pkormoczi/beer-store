package dev.ronin.demo.beerstore.infrastructure.security;

public class AuthorizationException extends RuntimeException{
    public AuthorizationException(String message) {
        super(message);
    }
}