package com.robert.portfolio.inventory.domain.model;

import com.robert.portfolio.inventory.domain.exception.InsufficientStockException;

public final class ProductStock {

    private final ProductId productId;
    private int availableQuantity;
    private int reservedQuantity;
    private final Long version;

    public ProductStock(ProductId productId, int availableQuantity, int reservedQuantity) {
        this(productId, availableQuantity, reservedQuantity, null);
    }

    public ProductStock(ProductId productId, int availableQuantity, int reservedQuantity, Long version) {
        if (availableQuantity < 0 || reservedQuantity < 0) {
            throw new IllegalArgumentException("Stock quantities cannot be negative");
        }
        this.productId = productId;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
        this.version = version;
    }

    public void reserve(StockQuantity quantity) {
        if (availableQuantity < quantity.value()) {
            throw new InsufficientStockException(productId);
        }
        availableQuantity -= quantity.value();
        reservedQuantity += quantity.value();
    }

    public ProductId productId() {
        return productId;
    }

    public int availableQuantity() {
        return availableQuantity;
    }

    public int reservedQuantity() {
        return reservedQuantity;
    }

    public Long version() {
        return version;
    }
}
