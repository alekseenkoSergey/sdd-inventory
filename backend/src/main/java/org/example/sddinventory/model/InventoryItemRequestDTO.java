package org.example.sddinventory.model;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class InventoryItemRequestDTO {
    @NotEmpty(message = "Name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Size(max = 100, message = "SKU cannot exceed 100 characters")
    private String sku;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Location ID is required")
    private Long locationId;

    @NotNull(message = "Unit is required")
    @Size(max = 50, message = "Unit cannot exceed 50 characters")
    private String unit;

    @PositiveOrZero(message = "Low stock threshold must be greater than or equal to 0")
    private BigDecimal lowStockThreshold;

    @PositiveOrZero(message = "Initial quantity must be greater than or equal to 0")
    private BigDecimal initialQuantity;

    public InventoryItemRequestDTO() {}

    public InventoryItemRequestDTO(String name, String description, String sku, Long categoryId,
                                   Long locationId, String unit, BigDecimal lowStockThreshold,
                                   BigDecimal initialQuantity) {
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.categoryId = categoryId;
        this.locationId = locationId;
        this.unit = unit;
        this.lowStockThreshold = lowStockThreshold;
        this.initialQuantity = initialQuantity;
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

    public BigDecimal getInitialQuantity() {
        return initialQuantity;
    }

    public void setInitialQuantity(BigDecimal initialQuantity) {
        this.initialQuantity = initialQuantity;
    }
}
