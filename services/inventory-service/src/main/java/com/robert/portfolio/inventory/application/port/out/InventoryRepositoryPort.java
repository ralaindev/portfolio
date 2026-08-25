package com.robert.portfolio.inventory.application.port.out;

import com.robert.portfolio.inventory.domain.model.ProductId;
import com.robert.portfolio.inventory.domain.model.ProductStock;

import java.util.Optional;

public interface InventoryRepositoryPort {

    Optional<ProductStock> findByProductId(ProductId productId);

    Optional<ProductStock> findByProductIdForUpdate(ProductId productId);

    ProductStock save(ProductStock productStock);
}
