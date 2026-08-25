package com.robert.portfolio.inventory.infrastructure.persistence;

import com.robert.portfolio.inventory.application.port.out.StockReservationRepositoryPort;
import com.robert.portfolio.inventory.domain.model.StockReservation;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class JpaStockReservationRepositoryAdapter implements StockReservationRepositoryPort {

    private final SpringDataStockReservationRepository repository;
    private final InventoryPersistenceMapper mapper;

    public JpaStockReservationRepositoryAdapter(SpringDataStockReservationRepository repository,
                                                InventoryPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<StockReservation> findByOrderIdAndProductId(UUID orderId, UUID productId) {
        return repository.findByOrderIdAndProductId(orderId, productId).map(mapper::toDomain);
    }

    @Override
    public StockReservation save(StockReservation reservation) {
        return mapper.toDomain(repository.save(mapper.toEntity(reservation)));
    }
}
