package com.robert.portfolio.inventory.domain.model;

import java.time.Instant;
import java.util.UUID;

public record StockReservation(
        UUID reservationId,
        UUID orderId,
        ProductId productId,
        StockQuantity quantity,
        Instant createdAt) {

    public StockReservation {
        if (reservationId == null || orderId == null || productId == null || quantity == null || createdAt == null) {
            throw new IllegalArgumentException("Stock reservation fields are required");
        }
    }
}
