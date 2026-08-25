package com.robert.portfolio.orders.infrastructure.web;

import com.robert.portfolio.orders.application.port.in.CreateOrderUseCase;
import com.robert.portfolio.orders.domain.model.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderWebMapper {

    public CreateOrderUseCase.CreateOrderCommand toCommand(CreateOrderRequest request, String idempotencyKey) {
        return new CreateOrderUseCase.CreateOrderCommand(
                request.customerId(),
                request.lines().stream()
                        .map(line -> new CreateOrderUseCase.CreateOrderCommand.Line(
                                line.productId(), line.quantity(), line.unitPrice()))
                        .toList(),
                idempotencyKey);
    }

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.id().value(),
                order.customerId().value(),
                order.status().name(),
                order.lines().stream()
                        .map(line -> new OrderResponse.OrderLineResponse(
                                line.productId().value(),
                                line.quantity().value(),
                                line.unitPrice().amount(),
                                line.subtotal().amount()))
                        .toList(),
                order.total().amount(),
                order.total().currency().getCurrencyCode(),
                order.createdAt(),
                order.updatedAt());
    }
}
