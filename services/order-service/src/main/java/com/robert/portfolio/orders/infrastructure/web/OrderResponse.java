package com.robert.portfolio.orders.infrastructure.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        UUID customerId,
        String status,
        List<OrderLineResponse> lines,
        BigDecimal total,
        String currency,
        Instant createdAt,
        Instant updatedAt) {

    public record OrderLineResponse(
            UUID productId,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal) {
    }
}
