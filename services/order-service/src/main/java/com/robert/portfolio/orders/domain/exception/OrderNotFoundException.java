package com.robert.portfolio.orders.domain.exception;

import com.robert.portfolio.orders.domain.model.OrderId;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(OrderId orderId) {
        super("Order %s was not found".formatted(orderId.value()));
    }
}
