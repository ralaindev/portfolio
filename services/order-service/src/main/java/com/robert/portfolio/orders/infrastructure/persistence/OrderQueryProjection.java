package com.robert.portfolio.orders.infrastructure.persistence;

import java.util.UUID;

public record OrderQueryProjection(UUID orderId, UUID customerId, long lineCount) {
}
