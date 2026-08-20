package org.example.sddinventory.service;

import org.example.sddinventory.entity.AdjustmentDirection;
import org.example.sddinventory.entity.InventoryItem;
import org.example.sddinventory.entity.ItemStatus;
import org.example.sddinventory.entity.MovementType;
import org.example.sddinventory.entity.StockMovement;
import org.example.sddinventory.exception.InvalidQuantityException;
import org.example.sddinventory.exception.NegativeQuantityException;
import org.example.sddinventory.model.StockMovementRequestDTO;
import org.example.sddinventory.model.StockMovementResponseDTO;
import org.example.sddinventory.repository.InventoryItemRepository;
import org.example.sddinventory.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockMovementServiceTest {
    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    private StockMovementServiceImpl stockMovementService;
    private InventoryItem testItem;

    @BeforeEach
    void setUp() {
        stockMovementService = new StockMovementServiceImpl(stockMovementRepository, inventoryItemRepository);

        testItem = new InventoryItem();
        testItem.setId(1L);
        testItem.setUserId(1L);
        testItem.setName("Test Item");
        testItem.setCurrentQuantity(BigDecimal.ZERO);
        testItem.setUnit("pcs");
        testItem.setStatus(ItemStatus.ACTIVE);
    }

    @Test
    void testCreateOpeningBalance() {
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> {
            StockMovement movement = invocation.getArgument(0);
            movement.setId(1L);
            return movement;
        });

        stockMovementService.createOpeningBalance(1L, BigDecimal.valueOf(100));

        verify(stockMovementRepository).save(any(StockMovement.class));
        verify(inventoryItemRepository).save(testItem);
        assertEquals(BigDecimal.valueOf(100), testItem.getCurrentQuantity());
    }

    @Test
    void testCreateOpeningBalanceWithZeroQuantity() {
        stockMovementService.createOpeningBalance(1L, BigDecimal.ZERO);

        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void testRecordStockInMovement() {
        testItem.setCurrentQuantity(BigDecimal.valueOf(100));
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> {
            StockMovement movement = invocation.getArgument(0);
            movement.setId(2L);
            movement.setCreatedDate(LocalDateTime.now(ZoneId.of("UTC")));
            return movement;
        });

        StockMovementRequestDTO requestDTO = new StockMovementRequestDTO(
            MovementType.STOCK_IN,
            50L,
            "Supplier delivery",
            LocalDate.now(),
            null
        );

        StockMovementResponseDTO response = stockMovementService.recordMovement(1L, requestDTO);

        assertNotNull(response);
        assertEquals(MovementType.STOCK_IN, response.getMovementType());
        assertEquals(BigDecimal.valueOf(150), testItem.getCurrentQuantity());
    }

    @Test
    void testRecordStockOutMovement() {
        testItem.setCurrentQuantity(BigDecimal.valueOf(100));
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> {
            StockMovement movement = invocation.getArgument(0);
            movement.setId(3L);
            movement.setCreatedDate(LocalDateTime.now(ZoneId.of("UTC")));
            return movement;
        });

        StockMovementRequestDTO requestDTO = new StockMovementRequestDTO(
            MovementType.STOCK_OUT,
            30L,
            "Sale",
            LocalDate.now(),
            null
        );

        StockMovementResponseDTO response = stockMovementService.recordMovement(1L, requestDTO);

        assertNotNull(response);
        assertEquals(MovementType.STOCK_OUT, response.getMovementType());
        assertEquals(BigDecimal.valueOf(70), testItem.getCurrentQuantity());
    }

    @Test
    void testRecordStockOutMovementNegativeQuantity() {
        testItem.setCurrentQuantity(BigDecimal.valueOf(100));
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        StockMovementRequestDTO requestDTO = new StockMovementRequestDTO(
            MovementType.STOCK_OUT,
            150L,
            null,
            LocalDate.now(),
            null
        );

        assertThrows(NegativeQuantityException.class, () -> {
            stockMovementService.recordMovement(1L, requestDTO);
        });
    }

    @Test
    void testRecordAdjustmentIncreaseMovement() {
        testItem.setCurrentQuantity(BigDecimal.valueOf(100));
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> {
            StockMovement movement = invocation.getArgument(0);
            movement.setId(4L);
            movement.setCreatedDate(LocalDateTime.now(ZoneId.of("UTC")));
            return movement;
        });

        StockMovementRequestDTO requestDTO = new StockMovementRequestDTO(
            MovementType.ADJUSTMENT,
            10L,
            "Physical count discrepancy",
            LocalDate.now(),
            AdjustmentDirection.INCREASE
        );

        StockMovementResponseDTO response = stockMovementService.recordMovement(1L, requestDTO);

        assertNotNull(response);
        assertEquals(MovementType.ADJUSTMENT, response.getMovementType());
        assertEquals(AdjustmentDirection.INCREASE, response.getAdjustmentDirection());
        assertEquals(BigDecimal.valueOf(110), testItem.getCurrentQuantity());
    }

    @Test
    void testRecordAdjustmentDecreaseMovement() {
        testItem.setCurrentQuantity(BigDecimal.valueOf(100));
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> {
            StockMovement movement = invocation.getArgument(0);
            movement.setId(5L);
            movement.setCreatedDate(LocalDateTime.now(ZoneId.of("UTC")));
            return movement;
        });

        StockMovementRequestDTO requestDTO = new StockMovementRequestDTO(
            MovementType.ADJUSTMENT,
            5L,
            "Inventory shrinkage",
            LocalDate.now(),
            AdjustmentDirection.DECREASE
        );

        StockMovementResponseDTO response = stockMovementService.recordMovement(1L, requestDTO);

        assertNotNull(response);
        assertEquals(MovementType.ADJUSTMENT, response.getMovementType());
        assertEquals(AdjustmentDirection.DECREASE, response.getAdjustmentDirection());
        assertEquals(BigDecimal.valueOf(95), testItem.getCurrentQuantity());
    }

    @Test
    void testRecordAdjustmentDecreaseNegativeQuantity() {
        testItem.setCurrentQuantity(BigDecimal.valueOf(100));
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        StockMovementRequestDTO requestDTO = new StockMovementRequestDTO(
            MovementType.ADJUSTMENT,
            200L,
            null,
            LocalDate.now(),
            AdjustmentDirection.DECREASE
        );

        assertThrows(NegativeQuantityException.class, () -> {
            stockMovementService.recordMovement(1L, requestDTO);
        });
    }

    @Test
    void testRecordMovementInvalidQuantity() {
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        StockMovementRequestDTO requestDTO = new StockMovementRequestDTO(
            MovementType.STOCK_IN,
            0L,
            null,
            LocalDate.now(),
            null
        );

        assertThrows(InvalidQuantityException.class, () -> {
            stockMovementService.recordMovement(1L, requestDTO);
        });
    }

    @Test
    void testRecordMovementMissingAdjustmentDirection() {
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        StockMovementRequestDTO requestDTO = new StockMovementRequestDTO(
            MovementType.ADJUSTMENT,
            10L,
            null,
            LocalDate.now(),
            null
        );

        assertThrows(IllegalArgumentException.class, () -> {
            stockMovementService.recordMovement(1L, requestDTO);
        });
    }

    @Test
    void testGetMovementHistory() {
        List<StockMovement> movements = new ArrayList<>();
        StockMovement movement1 = new StockMovement();
        movement1.setId(1L);
        movement1.setItemId(1L);
        movement1.setMovementType(MovementType.OPENING_BALANCE);
        movement1.setQuantity(100L);
        movement1.setMovementDate(LocalDate.now());
        movement1.setCreatedDate(LocalDateTime.now(ZoneId.of("UTC")));

        movements.add(movement1);

        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(stockMovementRepository.findByItemIdOrderByCreatedDateAsc(1L)).thenReturn(movements);

        List<StockMovementResponseDTO> history = stockMovementService.getMovementHistory(1L);

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(MovementType.OPENING_BALANCE, history.get(0).getMovementType());
    }

    @Test
    void testGetMovementHistoryByDateRange() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        List<StockMovement> movements = new ArrayList<>();
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(stockMovementRepository.findByItemIdAndDateRange(1L, startDate, endDate)).thenReturn(movements);

        List<StockMovementResponseDTO> history = stockMovementService.getMovementHistoryByDateRange(1L, startDate, endDate);

        assertNotNull(history);
        assertEquals(0, history.size());
    }
}
