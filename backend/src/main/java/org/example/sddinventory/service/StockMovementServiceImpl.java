package org.example.sddinventory.service;

import org.example.sddinventory.entity.AdjustmentDirection;
import org.example.sddinventory.entity.InventoryItem;
import org.example.sddinventory.entity.MovementType;
import org.example.sddinventory.entity.StockMovement;
import org.example.sddinventory.exception.InvalidQuantityException;
import org.example.sddinventory.exception.NegativeQuantityException;
import org.example.sddinventory.model.StockMovementRequestDTO;
import org.example.sddinventory.model.StockMovementResponseDTO;
import org.example.sddinventory.repository.InventoryItemRepository;
import org.example.sddinventory.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StockMovementServiceImpl implements StockMovementService {
    private final StockMovementRepository stockMovementRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public StockMovementServiceImpl(StockMovementRepository stockMovementRepository,
                                   InventoryItemRepository inventoryItemRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @Override
    public void createOpeningBalance(Long itemId, BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        InventoryItem item = inventoryItemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        LocalDate movementDate = LocalDate.now();
        LocalDateTime createdDate = LocalDateTime.now(ZoneId.of("UTC"));

        StockMovement movement = new StockMovement(
            itemId,
            MovementType.OPENING_BALANCE,
            quantity.longValue(),
            null,
            null,
            movementDate,
            createdDate
        );

        StockMovement savedMovement = stockMovementRepository.save(movement);
        item.updateCurrentQuantityFromMovement(savedMovement);
        inventoryItemRepository.save(item);
    }

    @Override
    public StockMovementResponseDTO recordMovement(Long itemId, StockMovementRequestDTO requestDTO) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        requestDTO.validateForMovementType();

        if (requestDTO.getQuantity() == null || requestDTO.getQuantity() <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }

        LocalDate movementDate = requestDTO.getMovementDate() != null ? requestDTO.getMovementDate() : LocalDate.now();
        LocalDateTime createdDate = LocalDateTime.now(ZoneId.of("UTC"));

        StockMovement movement = new StockMovement(
            itemId,
            requestDTO.getMovementType(),
            requestDTO.getQuantity(),
            requestDTO.getAdjustmentDirection(),
            requestDTO.getReason(),
            movementDate,
            createdDate
        );

        try {
            item.validateMovement(movement);
        } catch (IllegalArgumentException e) {
            if (movement.getMovementType() == MovementType.STOCK_OUT ||
                (movement.getMovementType() == MovementType.ADJUSTMENT && movement.getAdjustmentDirection() == AdjustmentDirection.DECREASE)) {
                throw new NegativeQuantityException(e.getMessage());
            }
            throw new InvalidQuantityException(e.getMessage());
        }

        StockMovement savedMovement = stockMovementRepository.save(movement);
        item.updateCurrentQuantityFromMovement(savedMovement);
        inventoryItemRepository.save(item);

        return StockMovementResponseDTO.fromEntity(savedMovement, item.getCurrentQuantity());
    }

    @Override
    public List<StockMovementResponseDTO> getMovementHistory(Long itemId) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        List<StockMovement> movements = stockMovementRepository.findByItemIdOrderByCreatedDateAsc(itemId);
        return movements.stream()
            .map(m -> StockMovementResponseDTO.fromEntity(m, item.getCurrentQuantity()))
            .collect(Collectors.toList());
    }

    @Override
    public List<StockMovementResponseDTO> getMovementHistoryByDateRange(Long itemId, LocalDate startDate, LocalDate endDate) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        List<StockMovement> movements = stockMovementRepository.findByItemIdAndDateRange(itemId, startDate, endDate);
        return movements.stream()
            .map(m -> StockMovementResponseDTO.fromEntity(m, item.getCurrentQuantity()))
            .collect(Collectors.toList());
    }
}
