package com.robert.portfolio.orders.domain.model;

import com.robert.portfolio.orders.domain.exception.InvalidOrderException;

import java.time.Instant;
import java.util.List;

public final class Order {

    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderLine> lines;
    private final Money total;
    private final IdempotencyKey idempotencyKey;
    private final String requestFingerprint;
    private final Instant createdAt;
    private final Instant updatedAt;
    private OrderStatus status;

    private Order(OrderId id,
                  CustomerId customerId,
                  List<OrderLine> lines,
                  IdempotencyKey idempotencyKey,
                  String requestFingerprint,
                  Instant createdAt) {
        if (lines == null || lines.isEmpty()) {
            throw new InvalidOrderException("An order must contain at least one line");
        }
        if (idempotencyKey == null || requestFingerprint == null || requestFingerprint.isBlank()) {
            throw new InvalidOrderException("Idempotency information is required");
        }
        this.id = id;
        this.customerId = customerId;
        this.lines = List.copyOf(lines);
        this.total = calculateTotal(this.lines);
        this.idempotencyKey = idempotencyKey;
        this.requestFingerprint = requestFingerprint;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.status = OrderStatus.PENDING;
    }

    public static Order create(OrderId id,
                               CustomerId customerId,
                               List<OrderLine> lines,
                               IdempotencyKey idempotencyKey,
                               String requestFingerprint,
                               Instant createdAt) {
        if (id == null || customerId == null || createdAt == null) {
            throw new InvalidOrderException("Order identity and creation time are required");
        }
        return new Order(id, customerId, lines, idempotencyKey, requestFingerprint, createdAt);
    }

    private static Money calculateTotal(List<OrderLine> lines) {
        return lines.stream()
            .map(OrderLine::subtotal)
            .reduce(Money::add)
            .orElseThrow();
    }

    public OrderId id() {
        return id;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public List<OrderLine> lines() {
        return lines;
    }

    public Money total() {
        return total;
    }

    public IdempotencyKey idempotencyKey() {
        return idempotencyKey;
    }

    public String requestFingerprint() {
        return requestFingerprint;
    }

    public OrderStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
