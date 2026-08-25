package com.robert.portfolio.orders.domain.model;

public record OrderLine(ProductId productId, Quantity quantity, Money unitPrice) {

    public OrderLine {
        if (productId == null || quantity == null || unitPrice == null) {
            throw new IllegalArgumentException("Order line fields are required");
        }
    }

    public Money subtotal() {
        return unitPrice.multiply(quantity);
    }
}
