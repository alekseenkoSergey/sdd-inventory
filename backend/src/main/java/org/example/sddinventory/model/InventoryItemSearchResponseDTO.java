package org.example.sddinventory.model;

import java.math.BigDecimal;
import java.time.Instant;

public class InventoryItemSearchResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private String categoryName;
    private Long categoryId;
    private String locationName;
    private Long locationId;
    private BigDecimal currentQuantity;
    private String unit;
    private BigDecimal lowStockThreshold;
    private Boolean isLowStock;
    private String status;
    private Instant createdDate;
    private Instant updatedDate;

    public InventoryItemSearchResponseDTO() {}

    public InventoryItemSearchResponseDTO(Long id, String name, String description, String sku,
                                         String categoryName, Long categoryId, String locationName, Long locationId,
                                         BigDecimal currentQuantity, String unit, BigDecimal lowStockThreshold,
                                         Boolean isLowStock, String status,
                                         Instant createdDate, Instant updatedDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.categoryName = categoryName;
        this.categoryId = categoryId;
        this.locationName = locationName;
        this.locationId = locationId;
        this.currentQuantity = currentQuantity;
        this.unit = unit;
        this.lowStockThreshold = lowStockThreshold;
        this.isLowStock = isLowStock;
        this.status = status;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
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

    public Boolean getIsLowStock() {
        return isLowStock;
    }

    public void setIsLowStock(Boolean isLowStock) {
        this.isLowStock = isLowStock;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
