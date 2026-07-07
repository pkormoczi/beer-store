package dev.ronin.demo.beerstore.platform.security;

public class AuthorizationException extends RuntimeException{
    public AuthorizationException(String message) {
        super(message);
    }
}