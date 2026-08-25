package com.robert.portfolio.orders.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface OutboxPort {

    void append(OrderCreatedMessage message);

    record OrderCreatedMessage(UUID orderId, Instant occurredAt) {
    }
}
