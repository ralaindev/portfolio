package com.robert.portfolio.orders.infrastructure.persistence;

import com.robert.portfolio.orders.domain.model.CustomerId;
import com.robert.portfolio.orders.domain.model.IdempotencyKey;
import com.robert.portfolio.orders.domain.model.Money;
import com.robert.portfolio.orders.domain.model.Order;
import com.robert.portfolio.orders.domain.model.OrderId;
import com.robert.portfolio.orders.domain.model.OrderLine;
import com.robert.portfolio.orders.domain.model.ProductId;
import com.robert.portfolio.orders.domain.model.Quantity;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.UUID;

@Component
public class OrderPersistenceMapper {

    public OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity(
                order.id().value(),
                order.customerId().value(),
                OrderStatusJpa.valueOf(order.status().name()),
                order.total().amount(),
                order.total().currency().getCurrencyCode(),
                order.idempotencyKey().value(),
                order.requestFingerprint(),
                order.createdAt(),
                order.updatedAt());
        order.lines().forEach(line -> entity.addLine(toEntity(line)));
        return entity;
    }

    public Order toDomain(OrderJpaEntity entity) {
        var lines = entity.getLines().stream()
                .map(this::toDomain)
                .toList();
        return Order.create(
                new OrderId(entity.getId()),
                new CustomerId(entity.getCustomerId()),
                lines,
                new IdempotencyKey(entity.getIdempotencyKey()),
                entity.getRequestFingerprint(),
                entity.getCreatedAt());
    }

    private OrderLineJpaEntity toEntity(OrderLine line) {
        return new OrderLineJpaEntity(
                UUID.randomUUID(),
                line.productId().value(),
                line.quantity().value(),
                line.unitPrice().amount(),
                line.unitPrice().currency().getCurrencyCode());
    }

    private OrderLine toDomain(OrderLineJpaEntity entity) {
        return new OrderLine(
                new ProductId(entity.getProductId()),
                new Quantity(entity.getQuantity()),
                new Money(entity.getUnitPrice(), Currency.getInstance(entity.getCurrency())));
    }
}
