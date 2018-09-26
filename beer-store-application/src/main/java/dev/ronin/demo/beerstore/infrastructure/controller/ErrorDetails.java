package dev.ronin.demo.beerstore.infrastructure.controller;

import lombok.Data;

import java.time.LocalDateTime;

@Data
class ErrorDetails {
    private LocalDateTime timestamp;
    private String message;
    private String details;

    public ErrorDetails() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorDetails(final String message, final String details) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.details = details;
    }
}
