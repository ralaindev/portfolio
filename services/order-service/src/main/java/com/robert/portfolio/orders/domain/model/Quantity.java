package com.robert.portfolio.orders.domain.model;

public record Quantity(int value) {

    public Quantity {
        if (value < 1) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }
}
