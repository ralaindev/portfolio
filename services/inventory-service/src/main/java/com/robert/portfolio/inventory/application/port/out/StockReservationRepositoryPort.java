package com.robert.portfolio.inventory.application.port.out;

import com.robert.portfolio.inventory.domain.model.StockReservation;

import java.util.Optional;
import java.util.UUID;

public interface StockReservationRepositoryPort {

    Optional<StockReservation> findByOrderIdAndProductId(UUID orderId, UUID productId);

    StockReservation save(StockReservation reservation);
}
