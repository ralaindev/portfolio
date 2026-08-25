package com.robert.portfolio.inventory.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

public record StockReservationResponse(
        UUID reservationId,
        UUID orderId,
        UUID productId,
        int quantity,
        Instant createdAt) {
}
