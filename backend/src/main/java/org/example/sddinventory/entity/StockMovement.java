package org.example.sddinventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movement", indexes = {
    @Index(name = "idx_stock_movement_item_date", columnList = "item_id, movement_date"),
    @Index(name = "idx_stock_movement_created", columnList = "created_date")
})
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @NotNull
    @Positive(message = "Quantity must be greater than 0")
    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_direction", nullable = true)
    private AdjustmentDirection adjustmentDirection;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @NotNull
    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @NotNull
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    public StockMovement() {}

    public StockMovement(Long itemId, MovementType movementType, Long quantity,
                         AdjustmentDirection adjustmentDirection, String reason,
                         LocalDate movementDate, LocalDateTime createdDate) {
        this.itemId = itemId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.adjustmentDirection = adjustmentDirection;
        this.reason = reason;
        this.movementDate = movementDate;
        this.createdDate = createdDate;
    }

    @PrePersist
    private void validateAdjustmentDirection() {
        if (this.movementType == MovementType.ADJUSTMENT && this.adjustmentDirection == null) {
            throw new IllegalArgumentException(
                "adjustmentDirection is required for ADJUSTMENT movements"
            );
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public AdjustmentDirection getAdjustmentDirection() {
        return adjustmentDirection;
    }

    public void setAdjustmentDirection(AdjustmentDirection adjustmentDirection) {
        this.adjustmentDirection = adjustmentDirection;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDate movementDate) {
        this.movementDate = movementDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
