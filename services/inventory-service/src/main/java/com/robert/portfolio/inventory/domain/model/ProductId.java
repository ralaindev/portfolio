package com.robert.portfolio.inventory.domain.model;

import java.util.UUID;

public record ProductId(UUID value) {

    public ProductId {
        if (value == null) {
            throw new IllegalArgumentException("Product id is required");
        }
    }
}
