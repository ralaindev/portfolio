package com.robert.portfolio.orders.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robert.portfolio.orders.application.port.out.OutboxPort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class JpaOutboxAdapter implements OutboxPort {

    private final SpringDataOutboxRepository repository;
    private final ObjectMapper objectMapper;

    public JpaOutboxAdapter(SpringDataOutboxRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void append(OrderCreatedMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", message.orderId(),
                    "occurredAt", message.occurredAt()));
            repository.save(new OutboxJpaEntity(
                    UUID.randomUUID(),
                    "Order",
                    message.orderId(),
                    "OrderCreated",
                    payload,
                    message.occurredAt()));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize OrderCreated event", exception);
        }
    }
}
