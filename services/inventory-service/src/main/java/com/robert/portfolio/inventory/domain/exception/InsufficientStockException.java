package com.robert.portfolio.inventory.domain.exception;

import com.robert.portfolio.inventory.domain.model.ProductId;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(ProductId productId) {
        super("There is not enough stock for product %s".formatted(productId.value()));
    }
}
