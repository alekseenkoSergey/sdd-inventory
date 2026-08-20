package org.example.sddinventory.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.example.sddinventory.entity.AdjustmentDirection;
import org.example.sddinventory.entity.MovementType;
import java.time.LocalDate;

public class StockMovementRequestDTO {
    @NotNull(message = "Movement type is required")
    private MovementType movementType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Long quantity;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate movementDate;

    private AdjustmentDirection adjustmentDirection;

    public StockMovementRequestDTO() {}

    public StockMovementRequestDTO(MovementType movementType, Long quantity, String reason,
                                    LocalDate movementDate, AdjustmentDirection adjustmentDirection) {
        this.movementType = movementType;
        this.quantity = quantity;
        this.reason = reason;
        this.movementDate = movementDate;
        this.adjustmentDirection = adjustmentDirection;
    }

    public void validateForMovementType() {
        if (this.movementType == MovementType.ADJUSTMENT && this.adjustmentDirection == null) {
            throw new IllegalArgumentException(
                "adjustmentDirection is required for ADJUSTMENT movements"
            );
        }
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

    public AdjustmentDirection getAdjustmentDirection() {
        return adjustmentDirection;
    }

    public void setAdjustmentDirection(AdjustmentDirection adjustmentDirection) {
        this.adjustmentDirection = adjustmentDirection;
    }
}
