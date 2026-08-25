package com.robert.portfolio.orders.application.usecase;

import com.robert.portfolio.orders.application.port.in.CreateOrderUseCase.CreateOrderCommand;
import com.robert.portfolio.orders.application.port.out.OrderRepositoryPort;
import com.robert.portfolio.orders.application.port.out.OutboxPort;
import com.robert.portfolio.orders.domain.exception.IdempotencyConflictException;
import com.robert.portfolio.orders.domain.model.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class CreateOrderServiceTest {

    private final OrderRepositoryPort repository = mock(OrderRepositoryPort.class);
    private final OutboxPort outbox = mock(OutboxPort.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
    private final CreateOrderService service = new CreateOrderService(repository, outbox, clock);

    @BeforeEach
    void stubRepositorySave() {
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldCreatePendingOrderAndPersistIt() {
        when(repository.findByIdempotencyKey(any())).thenReturn(java.util.Optional.empty());

        Order result = service.create(command("key", 2));

        assertThat(result.status().name()).isEqualTo("PENDING");
        assertThat(result.total().amount()).isEqualByComparingTo("20.00");
        verify(repository).save(result);
        verify(outbox).append(any());
    }

    @Test
    void shouldReturnExistingOrderForTheSameIdempotencyKeyAndContent() {
        Order existing = service.create(command("key", 2));
        clearInvocations(repository, outbox);
        when(repository.findByIdempotencyKey(any())).thenReturn(java.util.Optional.of(existing));

        Order result = service.create(command("key", 2));

        assertThat(result).isSameAs(existing);
        verify(repository, never()).save(any());
        verify(outbox, never()).append(any());
    }

    @Test
    void shouldRejectTheSameIdempotencyKeyWithDifferentContent() {
        Order existing = service.create(command("key", 2));
        clearInvocations(repository, outbox);
        when(repository.findByIdempotencyKey(any())).thenReturn(java.util.Optional.of(existing));

        assertThatThrownBy(() -> service.create(command("key", 3)))
                .isInstanceOf(IdempotencyConflictException.class);
        verify(repository, never()).save(any());
        verify(outbox, never()).append(any());
    }

    private CreateOrderCommand command(String key, int quantity) {
        return new CreateOrderCommand(
                UUID.fromString("ad2f3742-a4a6-4b9c-aed1-45884cc7bd21"),
                List.of(new CreateOrderCommand.Line(
                        UUID.fromString("55db5caf-b0da-49ab-b346-b7581a91ee76"),
                        quantity,
                        new BigDecimal("10.00"))),
                key);
    }
}
