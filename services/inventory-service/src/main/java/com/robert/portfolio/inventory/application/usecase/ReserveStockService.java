package com.robert.portfolio.inventory.application.usecase;

import com.robert.portfolio.inventory.application.port.out.InventoryRepositoryPort;
import com.robert.portfolio.inventory.application.port.out.StockReservationRepositoryPort;
import com.robert.portfolio.inventory.domain.exception.StockReservationConflictException;
import com.robert.portfolio.inventory.domain.model.ProductId;
import com.robert.portfolio.inventory.domain.model.ProductStock;
import com.robert.portfolio.inventory.domain.model.StockQuantity;
import com.robert.portfolio.inventory.domain.model.StockReservation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class ReserveStockService {

    private final InventoryRepositoryPort inventoryRepository;
    private final StockReservationRepositoryPort reservationRepository;
    private final Clock clock;

    public ReserveStockService(InventoryRepositoryPort inventoryRepository,
                               StockReservationRepositoryPort reservationRepository,
                               Clock clock) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    @Transactional
    public StockReservation reserve(UUID orderId, UUID productId, int quantity) {
        // The normal path does not lock the row. @Version detects a collision when the update is flushed.
        var existing = reservationRepository.findByOrderIdAndProductId(orderId, productId);
        if (existing.isPresent()) {
            return existing.get();
        }

        ProductId product = new ProductId(productId);
        StockQuantity requestedQuantity = new StockQuantity(quantity);
        ProductStock stock = inventoryRepository.findByProductId(product)
                .orElseThrow(() -> new StockReservationConflictException("Product stock was not found"));
        stock.reserve(requestedQuantity);
        return persistReservation(orderId, product, requestedQuantity, stock);
    }

    @Transactional
    public StockReservation reservePessimistically(UUID orderId, UUID productId, int quantity) {
        // This alternative locks the row before changing it. Keep this transaction short to reduce waiting.
        var existing = reservationRepository.findByOrderIdAndProductId(orderId, productId);
        if (existing.isPresent()) {
            return existing.get();
        }

        ProductId product = new ProductId(productId);
        StockQuantity requestedQuantity = new StockQuantity(quantity);
        ProductStock stock = inventoryRepository.findByProductIdForUpdate(product)
                .orElseThrow(() -> new StockReservationConflictException("Product stock was not found"));
        stock.reserve(requestedQuantity);
        return persistReservation(orderId, product, requestedQuantity, stock);
    }

    private StockReservation persistReservation(UUID orderId,
                                                ProductId product,
                                                StockQuantity quantity,
                                                ProductStock stock) {
        try {
            // The stock update and reservation insert must commit or roll back together.
            inventoryRepository.save(stock);
            return reservationRepository.save(new StockReservation(
                    UUID.randomUUID(), orderId, product, quantity, Instant.now(clock)));
        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException exception) {
            throw new StockReservationConflictException("Stock reservation conflicted with another operation");
        }
    }
}
