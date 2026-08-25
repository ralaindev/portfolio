package com.robert.portfolio.orders.application.port.in;

import com.robert.portfolio.orders.domain.model.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CreateOrderUseCase {

    Order create(CreateOrderCommand command);

    record CreateOrderCommand(UUID customerId, List<Line> lines, String idempotencyKey) {
        public record Line(UUID productId, int quantity, BigDecimal unitPrice) {
        }
    }
}
