package com.robert.portfolio.inventory.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

interface SpringDataProductStockRepository extends JpaRepository<ProductStockJpaEntity, UUID> {

    // PESSIMISTIC_WRITE serializes transactions that reserve the same product row.
    // It is an explicit alternative to the default optimistic @Version strategy.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stock from ProductStockJpaEntity stock where stock.productId = :productId")
    Optional<ProductStockJpaEntity> findByProductIdForUpdate(UUID productId);
}
