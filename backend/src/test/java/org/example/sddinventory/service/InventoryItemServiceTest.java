package org.example.sddinventory.service;

import org.example.sddinventory.entity.Category;
import org.example.sddinventory.entity.InventoryItem;
import org.example.sddinventory.entity.ItemStatus;
import org.example.sddinventory.entity.Location;
import org.example.sddinventory.exception.*;
import org.example.sddinventory.model.InventoryItemPatchDTO;
import org.example.sddinventory.model.InventoryItemRequestDTO;
import org.example.sddinventory.model.InventoryItemResponseDTO;
import org.example.sddinventory.repository.CategoryRepository;
import org.example.sddinventory.repository.InventoryItemRepository;
import org.example.sddinventory.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryItemServiceTest {
    private InventoryItemService inventoryItemService;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private StockMovementService stockMovementService;

    private Long userId;
    private Long categoryId;
    private Long locationId;

    @BeforeEach
    public void setUp() {
        inventoryItemService = new InventoryItemServiceImpl(
            inventoryItemRepository,
            categoryRepository,
            locationRepository,
            stockMovementService
        );
        userId = 1L;
        categoryId = 100L;
        locationId = 200L;
    }

    @Test
    public void testCreateItemWithInitialQuantity() {
        InventoryItemRequestDTO request = new InventoryItemRequestDTO(
            "Test Item",
            "Description",
            "SKU-001",
            categoryId,
            locationId,
            "pcs",
            BigDecimal.TEN,
            BigDecimal.valueOf(100)
        );

        Category category = new Category();
        Location location = new Location();
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));
        when(locationRepository.findByIdAndUserId(locationId, userId)).thenReturn(Optional.of(location));
        when(inventoryItemRepository.findByUserIdAndSku(userId, "SKU-001")).thenReturn(Optional.empty());

        InventoryItem savedItem = new InventoryItem(
            userId,
            "Test Item",
            "Description",
            "SKU-001",
            categoryId,
            locationId,
            BigDecimal.valueOf(100),
            "pcs",
            BigDecimal.TEN,
            ItemStatus.ACTIVE
        );
        savedItem.setId(1L);
        savedItem.setCreatedDate(Instant.now());
        savedItem.setUpdatedDate(Instant.now());

        when(inventoryItemRepository.save(any())).thenReturn(savedItem);

        InventoryItemResponseDTO result = inventoryItemService.createItem(userId, request);

        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        assertEquals(BigDecimal.valueOf(100), result.getCurrentQuantity());
        assertEquals("ACTIVE", result.getStatus());
        verify(stockMovementService, times(1)).createOpeningBalance(1L, BigDecimal.valueOf(100));
        verify(inventoryItemRepository, times(1)).save(any());
    }

    @Test
    public void testCreateItemWithoutInitialQuantity() {
        InventoryItemRequestDTO request = new InventoryItemRequestDTO(
            "Test Item",
            "Description",
            "SKU-002",
            categoryId,
            locationId,
            "pcs",
            BigDecimal.TEN,
            null
        );

        Category category = new Category();
        Location location = new Location();
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));
        when(locationRepository.findByIdAndUserId(locationId, userId)).thenReturn(Optional.of(location));
        when(inventoryItemRepository.findByUserIdAndSku(userId, "SKU-002")).thenReturn(Optional.empty());

        InventoryItem savedItem = new InventoryItem(
            userId,
            "Test Item",
            "Description",
            "SKU-002",
            categoryId,
            locationId,
            BigDecimal.ZERO,
            "pcs",
            BigDecimal.TEN,
            ItemStatus.ACTIVE
        );
        savedItem.setId(1L);
        savedItem.setCreatedDate(Instant.now());
        savedItem.setUpdatedDate(Instant.now());

        when(inventoryItemRepository.save(any())).thenReturn(savedItem);

        InventoryItemResponseDTO result = inventoryItemService.createItem(userId, request);

        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        assertEquals(BigDecimal.ZERO, result.getCurrentQuantity());
        verify(stockMovementService, never()).createOpeningBalance(anyLong(), any());
    }

    @Test
    public void testCreateItemCategoryNotFound() {
        InventoryItemRequestDTO request = new InventoryItemRequestDTO(
            "Test Item",
            "Description",
            "SKU-003",
            999L,
            locationId,
            "pcs",
            BigDecimal.TEN,
            BigDecimal.valueOf(50)
        );

        when(categoryRepository.findByIdAndUserId(999L, userId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
            () -> inventoryItemService.createItem(userId, request));
        verify(inventoryItemRepository, never()).save(any());
    }

    @Test
    public void testCreateItemLocationNotFound() {
        InventoryItemRequestDTO request = new InventoryItemRequestDTO(
            "Test Item",
            "Description",
            "SKU-004",
            categoryId,
            999L,
            "pcs",
            BigDecimal.TEN,
            BigDecimal.valueOf(50)
        );

        Category category = new Category();
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));
        when(locationRepository.findByIdAndUserId(999L, userId)).thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class,
            () -> inventoryItemService.createItem(userId, request));
        verify(inventoryItemRepository, never()).save(any());
    }

    @Test
    public void testCreateItemSkuDuplicate() {
        InventoryItemRequestDTO request = new InventoryItemRequestDTO(
            "Test Item",
            "Description",
            "SKU-DUPLICATE",
            categoryId,
            locationId,
            "pcs",
            BigDecimal.TEN,
            BigDecimal.valueOf(50)
        );

        Category category = new Category();
        Location location = new Location();
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));
        when(locationRepository.findByIdAndUserId(locationId, userId)).thenReturn(Optional.of(location));
        when(inventoryItemRepository.findByUserIdAndSku(userId, "SKU-DUPLICATE"))
            .thenReturn(Optional.of(new InventoryItem()));

        assertThrows(SkuDuplicateException.class,
            () -> inventoryItemService.createItem(userId, request));
        verify(inventoryItemRepository, never()).save(any());
    }

    @Test
    public void testGetItemSuccess() {
        InventoryItem item = new InventoryItem(
            userId,
            "Test Item",
            "Description",
            "SKU-005",
            categoryId,
            locationId,
            BigDecimal.valueOf(100),
            "pcs",
            BigDecimal.TEN,
            ItemStatus.ACTIVE
        );
        item.setId(1L);
        item.setCreatedDate(Instant.now());
        item.setUpdatedDate(Instant.now());

        when(inventoryItemRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(item));

        InventoryItemResponseDTO result = inventoryItemService.getItem(userId, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Item", result.getName());
    }

    @Test
    public void testGetItemNotFound() {
        when(inventoryItemRepository.findByIdAndUserId(999L, userId)).thenReturn(Optional.empty());

        assertThrows(InventoryItemNotFoundException.class,
            () -> inventoryItemService.getItem(userId, 999L));
    }

    @Test
    public void testArchiveItem() {
        InventoryItem item = new InventoryItem(
            userId,
            "Test Item",
            "Description",
            "SKU-006",
            categoryId,
            locationId,
            BigDecimal.valueOf(100),
            "pcs",
            BigDecimal.TEN,
            ItemStatus.ACTIVE
        );
        item.setId(1L);
        item.setCreatedDate(Instant.now());
        item.setUpdatedDate(Instant.now());

        when(inventoryItemRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(item));

        InventoryItem archivedItem = new InventoryItem(
            userId,
            "Test Item",
            "Description",
            "SKU-006",
            categoryId,
            locationId,
            BigDecimal.valueOf(100),
            "pcs",
            BigDecimal.TEN,
            ItemStatus.ARCHIVED
        );
        archivedItem.setId(1L);
        archivedItem.setCreatedDate(Instant.now());
        archivedItem.setUpdatedDate(Instant.now());

        when(inventoryItemRepository.save(any())).thenReturn(archivedItem);

        InventoryItemResponseDTO result = inventoryItemService.archiveItem(userId, 1L);

        assertNotNull(result);
        assertEquals("ARCHIVED", result.getStatus());
    }

    @Test
    public void testDeleteItem() {
        InventoryItem item = new InventoryItem(
            userId,
            "Test Item",
            "Description",
            "SKU-008",
            categoryId,
            locationId,
            BigDecimal.valueOf(100),
            "pcs",
            BigDecimal.TEN,
            ItemStatus.ACTIVE
        );
        item.setId(1L);
        item.setCreatedDate(Instant.now());
        item.setUpdatedDate(Instant.now());

        when(inventoryItemRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(item));

        inventoryItemService.deleteItem(userId, 1L);

        verify(inventoryItemRepository, times(1)).deleteByIdAndUserId(1L, userId);
    }
}
