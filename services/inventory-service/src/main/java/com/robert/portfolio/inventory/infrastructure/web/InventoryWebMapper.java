package com.robert.portfolio.inventory.infrastructure.web;

import com.robert.portfolio.inventory.domain.model.StockReservation;
import org.springframework.stereotype.Component;

@Component
public class InventoryWebMapper {

    public StockReservationResponse toResponse(StockReservation reservation) {
        return new StockReservationResponse(
                reservation.reservationId(),
                reservation.orderId(),
                reservation.productId().value(),
                reservation.quantity().value(),
                reservation.createdAt());
    }
}
