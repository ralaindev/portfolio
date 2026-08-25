package com.robert.portfolio.inventory.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.UUID;

@Entity
@Table(name = "product_stock")
public class ProductStockJpaEntity {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    // The version is compared in the UPDATE WHERE clause to detect lost updates.
    @Version
    private Long version;

    protected ProductStockJpaEntity() {
    }

    public ProductStockJpaEntity(UUID productId, int availableQuantity, int reservedQuantity) {
        this(productId, availableQuantity, reservedQuantity, null);
    }

    public ProductStockJpaEntity(UUID productId, int availableQuantity, int reservedQuantity, Long version) {
        this.productId = productId;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
        this.version = version;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public Long getVersion() {
        return version;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }
}
