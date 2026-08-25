package com.robert.portfolio.inventory.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataStockReservationRepository extends JpaRepository<StockReservationJpaEntity, UUID> {

    Optional<StockReservationJpaEntity> findByOrderIdAndProductId(UUID orderId, UUID productId);
}
