package com.robert.portfolio.orders.application.usecase;

import com.robert.portfolio.orders.application.port.in.CreateOrderUseCase;
import com.robert.portfolio.orders.application.port.out.OrderRepositoryPort;
import com.robert.portfolio.orders.application.port.out.OutboxPort;
import com.robert.portfolio.orders.domain.exception.IdempotencyConflictException;
import com.robert.portfolio.orders.domain.model.CustomerId;
import com.robert.portfolio.orders.domain.model.IdempotencyKey;
import com.robert.portfolio.orders.domain.model.Money;
import com.robert.portfolio.orders.domain.model.Order;
import com.robert.portfolio.orders.domain.model.OrderId;
import com.robert.portfolio.orders.domain.model.OrderLine;
import com.robert.portfolio.orders.domain.model.ProductId;
import com.robert.portfolio.orders.domain.model.Quantity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class CreateOrderService implements CreateOrderUseCase {

    private static final java.util.Currency DEFAULT_CURRENCY = java.util.Currency.getInstance("EUR");

    private final OrderRepositoryPort orderRepository;
    private final OutboxPort outbox;
    private final Clock clock;

    public CreateOrderService(OrderRepositoryPort orderRepository, OutboxPort outbox, Clock clock) {
        this.orderRepository = orderRepository;
        this.outbox = outbox;
        this.clock = clock;
    }

    @Override
    // The order and all persistence work performed by the output port share this boundary.
    @Transactional
    public Order create(CreateOrderCommand command) {
        IdempotencyKey idempotencyKey = new IdempotencyKey(command.idempotencyKey());
        String fingerprint = fingerprint(command);

        // This check makes retries cheap, but the database unique constraint remains the final guarantee.
        var existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
        if (existingOrder.isPresent()) {
            return returnExistingOrReject(existingOrder.get(), fingerprint);
        }

        List<OrderLine> lines = command.lines().stream()
                .map(line -> new OrderLine(
                        new ProductId(line.productId()),
                        new Quantity(line.quantity()),
                        new Money(line.unitPrice(), DEFAULT_CURRENCY)))
                .toList();

        Order order = Order.create(
                new OrderId(UUID.randomUUID()),
                new CustomerId(command.customerId()),
                lines,
                idempotencyKey,
                fingerprint,
                Instant.now(clock));

        try {
            Order savedOrder = orderRepository.save(order);
            outbox.append(new OutboxPort.OrderCreatedMessage(savedOrder.id().value(), savedOrder.createdAt()));
            return savedOrder;
        } catch (DataIntegrityViolationException exception) {
            // Two requests can pass the previous lookup concurrently; translate the database conflict.
            throw new IdempotencyConflictException("The idempotency key is already being used");
        }
    }

    private Order returnExistingOrReject(Order existingOrder, String fingerprint) {
        if (!existingOrder.requestFingerprint().equals(fingerprint)) {
            throw new IdempotencyConflictException("The idempotency key was used with different content");
        }
        return existingOrder;
    }

    private String fingerprint(CreateOrderCommand command) {
        String canonicalValue = command.customerId()
                + "|"
                + command.lines().stream()
                .map(line -> line.productId() + ":" + line.quantity() + ":" + line.unitPrice().stripTrailingZeros().toPlainString())
                .reduce((left, right) -> left + ";" + right)
                .orElse("");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonicalValue.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
