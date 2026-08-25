package com.robert.portfolio.orders.domain.model;

import java.util.UUID;

public record OrderId(UUID value) {

    public OrderId {
        if (value == null) {
            throw new IllegalArgumentException("Order id is required");
        }
    }
}
