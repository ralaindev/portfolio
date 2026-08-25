package com.robert.portfolio.inventory.domain.model;

import com.robert.portfolio.inventory.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductStockTest {

    @Test
    void shouldMoveQuantityFromAvailableToReservedStock() {
        ProductStock stock = new ProductStock(new ProductId(UUID.randomUUID()), 5, 0);

        stock.reserve(new StockQuantity(2));

        assertThat(stock.availableQuantity()).isEqualTo(3);
        assertThat(stock.reservedQuantity()).isEqualTo(2);
    }

    @Test
    void shouldRejectReservationWhenAvailableStockIsInsufficient() {
        ProductStock stock = new ProductStock(new ProductId(UUID.randomUUID()), 1, 0);

        assertThatThrownBy(() -> stock.reserve(new StockQuantity(2)))
                .isInstanceOf(InsufficientStockException.class);
    }
}
