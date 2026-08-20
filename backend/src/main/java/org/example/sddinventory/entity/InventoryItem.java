package org.example.sddinventory.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inventory_item")
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 100)
    private String sku;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private Long locationId;

    @Column(nullable = false)
    private BigDecimal currentQuantity;

    @Column(nullable = false, length = 50)
    private String unit;

    @Column(nullable = false)
    private BigDecimal lowStockThreshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status;

    @CreationTimestamp
    @Column(nullable = false)
    private Instant createdDate;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedDate;

    public InventoryItem() {}

    public InventoryItem(Long userId, String name, String description, String sku, Long categoryId,
                         Long locationId, BigDecimal currentQuantity, String unit,
                         BigDecimal lowStockThreshold, ItemStatus status) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.categoryId = categoryId;
        this.locationId = locationId;
        this.currentQuantity = currentQuantity;
        this.unit = unit;
        this.lowStockThreshold = lowStockThreshold;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public BigDecimal getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(BigDecimal currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(BigDecimal lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }
}
