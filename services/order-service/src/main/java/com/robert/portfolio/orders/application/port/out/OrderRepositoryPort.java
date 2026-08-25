package com.robert.portfolio.orders.application.port.out;

import com.robert.portfolio.orders.domain.model.IdempotencyKey;
import com.robert.portfolio.orders.domain.model.Order;
import com.robert.portfolio.orders.domain.model.OrderId;

import java.util.Optional;

public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(OrderId orderId);

    Optional<Order> findByIdempotencyKey(IdempotencyKey idempotencyKey);
}
