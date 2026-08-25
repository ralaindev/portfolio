package com.robert.portfolio.orders.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull UUID customerId,
        @NotEmpty @Size(max = 100) List<@Valid OrderLineRequest> lines) {

    public record OrderLineRequest(
            @NotNull UUID productId,
            @Positive int quantity,
            @NotNull @DecimalMin(value = "0.01") BigDecimal unitPrice) {
    }
}
