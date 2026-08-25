package com.robert.portfolio.orders.domain.model;

public record IdempotencyKey(String value) {

    public IdempotencyKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Idempotency key cannot be blank");
        }
    }
}
