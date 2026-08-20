package org.example.sddinventory.controller;

import org.example.sddinventory.model.StockMovementRequestDTO;
import org.example.sddinventory.model.StockMovementResponseDTO;
import org.example.sddinventory.service.StockMovementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/items/{itemId}/movements")
public class StockMovementController {
    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @PostMapping
    public ResponseEntity<StockMovementResponseDTO> recordMovement(
        @PathVariable Long itemId,
        @Valid @RequestBody StockMovementRequestDTO requestDTO) {
        StockMovementResponseDTO response = stockMovementService.recordMovement(itemId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<StockMovementResponseDTO>> getMovementHistory(
        @PathVariable Long itemId,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        List<StockMovementResponseDTO> movements;
        if (startDate != null && endDate != null) {
            movements = stockMovementService.getMovementHistoryByDateRange(itemId, startDate, endDate);
        } else {
            movements = stockMovementService.getMovementHistory(itemId);
        }
        return ResponseEntity.ok(movements);
    }
}
