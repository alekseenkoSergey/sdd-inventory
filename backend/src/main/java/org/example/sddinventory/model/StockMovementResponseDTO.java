package org.example.sddinventory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.example.sddinventory.entity.AdjustmentDirection;
import org.example.sddinventory.entity.MovementType;
import org.example.sddinventory.entity.StockMovement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class StockMovementResponseDTO {
    private Long id;
    private Long itemId;
    private MovementType movementType;
    private Long quantity;
    private AdjustmentDirection adjustmentDirection;
    private String reason;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate movementDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdDate;

    private BigDecimal itemCurrentQuantity;

    public StockMovementResponseDTO() {}

    public StockMovementResponseDTO(Long id, Long itemId, MovementType movementType, Long quantity,
                                    AdjustmentDirection adjustmentDirection, String reason,
                                    LocalDate movementDate, LocalDateTime createdDate, BigDecimal itemCurrentQuantity) {
        this.id = id;
        this.itemId = itemId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.adjustmentDirection = adjustmentDirection;
        this.reason = reason;
        this.movementDate = movementDate;
        this.createdDate = createdDate;
        this.itemCurrentQuantity = itemCurrentQuantity;
    }

    public static StockMovementResponseDTO fromEntity(StockMovement movement, BigDecimal currentQuantity) {
        return new StockMovementResponseDTO(
            movement.getId(),
            movement.getItemId(),
            movement.getMovementType(),
            movement.getQuantity(),
            movement.getAdjustmentDirection(),
            movement.getReason(),
            movement.getMovementDate(),
            movement.getCreatedDate(),
            currentQuantity
        );
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

    public BigDecimal getItemCurrentQuantity() {
        return itemCurrentQuantity;
    }

    public void setItemCurrentQuantity(BigDecimal itemCurrentQuantity) {
        this.itemCurrentQuantity = itemCurrentQuantity;
    }
}
