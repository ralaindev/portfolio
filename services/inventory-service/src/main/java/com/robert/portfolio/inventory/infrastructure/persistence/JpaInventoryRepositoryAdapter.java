package com.robert.portfolio.inventory.infrastructure.persistence;

import com.robert.portfolio.inventory.application.port.out.InventoryRepositoryPort;
import com.robert.portfolio.inventory.domain.model.ProductId;
import com.robert.portfolio.inventory.domain.model.ProductStock;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaInventoryRepositoryAdapter implements InventoryRepositoryPort {

    private final SpringDataProductStockRepository repository;
    private final InventoryPersistenceMapper mapper;

    public JpaInventoryRepositoryAdapter(SpringDataProductStockRepository repository,
                                         InventoryPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ProductStock> findByProductId(ProductId productId) {
        return repository.findById(productId.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<ProductStock> findByProductIdForUpdate(ProductId productId) {
        return repository.findByProductIdForUpdate(productId.value()).map(mapper::toDomain);
    }

    @Override
    public ProductStock save(ProductStock productStock) {
        return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(productStock)));
    }
}
