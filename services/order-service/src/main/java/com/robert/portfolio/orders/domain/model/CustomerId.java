package com.robert.portfolio.orders.domain.model;

import java.util.UUID;

public record CustomerId(UUID value) {

    public CustomerId {
        if (value == null) {
            throw new IllegalArgumentException("Customer id is required");
        }
    }
}
