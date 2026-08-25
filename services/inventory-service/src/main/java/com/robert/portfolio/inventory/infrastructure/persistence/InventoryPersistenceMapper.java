package com.robert.portfolio.inventory.infrastructure.persistence;

import com.robert.portfolio.inventory.domain.model.ProductId;
import com.robert.portfolio.inventory.domain.model.ProductStock;
import com.robert.portfolio.inventory.domain.model.StockQuantity;
import com.robert.portfolio.inventory.domain.model.StockReservation;
import org.springframework.stereotype.Component;

@Component
public class InventoryPersistenceMapper {

    public ProductStock toDomain(ProductStockJpaEntity entity) {
        return new ProductStock(
                new ProductId(entity.getProductId()),
                entity.getAvailableQuantity(),
                entity.getReservedQuantity(),
                entity.getVersion());
    }

    public ProductStockJpaEntity toEntity(ProductStock stock) {
        return new ProductStockJpaEntity(
                stock.productId().value(),
                stock.availableQuantity(),
                stock.reservedQuantity(),
                stock.version());
    }

    public StockReservation toDomain(StockReservationJpaEntity entity) {
        return new StockReservation(
                entity.getId(),
                entity.getOrderId(),
                new ProductId(entity.getProductId()),
                new StockQuantity(entity.getQuantity()),
                entity.getCreatedAt());
    }

    public StockReservationJpaEntity toEntity(StockReservation reservation) {
        return new StockReservationJpaEntity(
                reservation.reservationId(),
                reservation.orderId(),
                reservation.productId().value(),
                reservation.quantity().value(),
                reservation.createdAt());
    }
}
