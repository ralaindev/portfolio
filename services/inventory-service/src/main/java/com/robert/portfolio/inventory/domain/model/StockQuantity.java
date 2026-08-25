package com.robert.portfolio.inventory.domain.model;

public record StockQuantity(int value) {

    public StockQuantity {
        if (value < 1) {
            throw new IllegalArgumentException("Stock quantity must be greater than zero");
        }
    }
}
