package org.example.sddinventory.service;

import org.example.sddinventory.model.StockMovementRequestDTO;
import org.example.sddinventory.model.StockMovementResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface StockMovementService {
    void createOpeningBalance(Long itemId, BigDecimal quantity);

    StockMovementResponseDTO recordMovement(Long itemId, StockMovementRequestDTO requestDTO);

    List<StockMovementResponseDTO> getMovementHistory(Long itemId);

    List<StockMovementResponseDTO> getMovementHistoryByDateRange(Long itemId, LocalDate startDate, LocalDate endDate);
}
