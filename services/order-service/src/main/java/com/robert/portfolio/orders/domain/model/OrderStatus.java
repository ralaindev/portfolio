package com.robert.portfolio.orders.domain.model;

public enum OrderStatus {
    PENDING,
    STOCK_RESERVED,
    PAYMENT_PENDING,
    PAYMENT_AUTHORIZED,
    CONFIRMED,
    CANCELLING,
    CANCELLED
}
