package com.robert.portfolio.orders.infrastructure.persistence;

public enum OrderStatusJpa {
    PENDING,
    STOCK_RESERVED,
    PAYMENT_PENDING,
    PAYMENT_AUTHORIZED,
    CONFIRMED,
    CANCELLING,
    CANCELLED
}
