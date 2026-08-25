package com.robert.portfolio.orders.domain.model;

import com.robert.portfolio.orders.domain.exception.InvalidOrderException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void shouldCalculateTotalFromAllOrderLines() {
        Order order = Order.create(
                new OrderId(UUID.randomUUID()),
                new CustomerId(UUID.randomUUID()),
                List.of(
                        line(2, "19.99"),
                        line(1, "5.00")),
                new IdempotencyKey("test-key"),
                "fingerprint",
                Instant.parse("2026-01-01T00:00:00Z"));

        assertThat(order.total()).isEqualTo(new Money(new BigDecimal("44.98"), EUR));
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void shouldRejectAnOrderWithoutLines() {
        assertThatThrownBy(() -> Order.create(
                new OrderId(UUID.randomUUID()),
                new CustomerId(UUID.randomUUID()),
                List.of(),
                new IdempotencyKey("test-key"),
                "fingerprint",
                Instant.now()))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessage("An order must contain at least one line");
    }

    @Test
    void shouldRejectNonPositiveQuantity() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Quantity(0))
                .withMessage("Quantity must be greater than zero");
    }

    @Test
    void shouldRejectNegativeMoney() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Money(new BigDecimal("-0.01"), EUR))
                .withMessage("Money amount cannot be negative");
    }

    private OrderLine line(int quantity, String unitPrice) {
        return new OrderLine(
                new ProductId(UUID.randomUUID()),
                new Quantity(quantity),
                new Money(new BigDecimal(unitPrice), EUR));
    }
}
