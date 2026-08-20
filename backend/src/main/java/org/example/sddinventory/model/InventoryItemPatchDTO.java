package org.example.sddinventory.model;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class InventoryItemPatchDTO {
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Size(max = 100, message = "SKU cannot exceed 100 characters")
    private String sku;

    private Long categoryId;

    private Long locationId;

    @Size(max = 50, message = "Unit cannot exceed 50 characters")
    private String unit;

    @PositiveOrZero(message = "Low stock threshold must be greater than or equal to 0")
    private BigDecimal lowStockThreshold;

    public InventoryItemPatchDTO() {}

    public InventoryItemPatchDTO(String name, String description, String sku, Long categoryId,
                                Long locationId, String unit, BigDecimal lowStockThreshold) {
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.categoryId = categoryId;
        this.locationId = locationId;
        this.unit = unit;
        this.lowStockThreshold = lowStockThreshold;
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
}
