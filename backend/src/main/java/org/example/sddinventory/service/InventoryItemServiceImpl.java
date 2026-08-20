package org.example.sddinventory.service;

import org.example.sddinventory.entity.InventoryItem;
import org.example.sddinventory.entity.ItemStatus;
import org.example.sddinventory.exception.CategoryNotFoundException;
import org.example.sddinventory.exception.InventoryItemNotFoundException;
import org.example.sddinventory.exception.LocationNotFoundException;
import org.example.sddinventory.exception.SkuDuplicateException;
import org.example.sddinventory.model.InventoryItemPatchDTO;
import org.example.sddinventory.model.InventoryItemRequestDTO;
import org.example.sddinventory.model.InventoryItemResponseDTO;
import org.example.sddinventory.repository.CategoryRepository;
import org.example.sddinventory.repository.InventoryItemRepository;
import org.example.sddinventory.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class InventoryItemServiceImpl implements InventoryItemService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryItemServiceImpl.class);
    private final InventoryItemRepository inventoryItemRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final StockMovementService stockMovementService;

    public InventoryItemServiceImpl(InventoryItemRepository inventoryItemRepository,
                                   CategoryRepository categoryRepository,
                                   LocationRepository locationRepository,
                                   StockMovementService stockMovementService) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.stockMovementService = stockMovementService;
    }

    @Override
    @Transactional
    public InventoryItemResponseDTO createItem(Long userId, InventoryItemRequestDTO requestDTO) {
        logger.debug("Creating inventory item: userId={}, name={}, sku={}", userId, requestDTO.getName(), requestDTO.getSku());

        validateCategory(requestDTO.getCategoryId(), userId);
        validateLocation(requestDTO.getLocationId(), userId);

        if (requestDTO.getSku() != null) {
            if (inventoryItemRepository.findByUserIdAndSku(userId, requestDTO.getSku()).isPresent()) {
                logger.warn("SKU duplicate: userId={}, sku={}", userId, requestDTO.getSku());
                throw new SkuDuplicateException("SKU already exists for this user");
            }
        }

        BigDecimal initialQuantity = requestDTO.getInitialQuantity() != null ?
            requestDTO.getInitialQuantity() : BigDecimal.ZERO;

        InventoryItem item = new InventoryItem(
            userId,
            requestDTO.getName(),
            requestDTO.getDescription(),
            requestDTO.getSku(),
            requestDTO.getCategoryId(),
            requestDTO.getLocationId(),
            initialQuantity,
            requestDTO.getUnit(),
            requestDTO.getLowStockThreshold() != null ?
                requestDTO.getLowStockThreshold() : BigDecimal.ZERO,
            ItemStatus.ACTIVE
        );

        InventoryItem saved = inventoryItemRepository.save(item);
        logger.info("Inventory item created: id={}, userId={}, name={}", saved.getId(), userId, saved.getName());

        if (initialQuantity.compareTo(BigDecimal.ZERO) > 0) {
            stockMovementService.createOpeningBalance(saved.getId(), initialQuantity);
            logger.info("Opening balance created for item: id={}, quantity={}", saved.getId(), initialQuantity);
        }

        return convertToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItemResponseDTO getItem(Long userId, Long itemId) {
        logger.debug("Fetching inventory item: userId={}, itemId={}", userId, itemId);
        InventoryItem item = inventoryItemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> {
                logger.warn("Item not found: userId={}, itemId={}", userId, itemId);
                return new InventoryItemNotFoundException("Item not found");
            });
        return convertToDTO(item);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryItemResponseDTO> listItems(Long userId, int page, int size, ItemStatus status) {
        logger.debug("Listing inventory items: userId={}, page={}, size={}, status={}", userId, page, size, status);
        Pageable pageable = PageRequest.of(page, size);
        Page<InventoryItem> items;

        if (status != null) {
            items = inventoryItemRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            items = inventoryItemRepository.findByUserId(userId, pageable);
        }

        return items.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public InventoryItemResponseDTO updateItem(Long userId, Long itemId, InventoryItemPatchDTO patchDTO) {
        logger.debug("Updating inventory item: userId={}, itemId={}", userId, itemId);

        InventoryItem item = inventoryItemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> {
                logger.warn("Item not found for update: userId={}, itemId={}", userId, itemId);
                return new InventoryItemNotFoundException("Item not found");
            });

        if (patchDTO.getName() != null) {
            item.setName(patchDTO.getName());
        }
        if (patchDTO.getDescription() != null) {
            item.setDescription(patchDTO.getDescription());
        }
        if (patchDTO.getSku() != null) {
            if (!patchDTO.getSku().equals(item.getSku())) {
                if (inventoryItemRepository.findByUserIdAndSku(userId, patchDTO.getSku()).isPresent()) {
                    logger.warn("SKU duplicate on update: userId={}, sku={}", userId, patchDTO.getSku());
                    throw new SkuDuplicateException("SKU already exists for this user");
                }
            }
            item.setSku(patchDTO.getSku());
        }
        if (patchDTO.getCategoryId() != null) {
            validateCategory(patchDTO.getCategoryId(), userId);
            item.setCategoryId(patchDTO.getCategoryId());
        }
        if (patchDTO.getLocationId() != null) {
            validateLocation(patchDTO.getLocationId(), userId);
            item.setLocationId(patchDTO.getLocationId());
        }
        if (patchDTO.getUnit() != null) {
            item.setUnit(patchDTO.getUnit());
        }
        if (patchDTO.getLowStockThreshold() != null) {
            item.setLowStockThreshold(patchDTO.getLowStockThreshold());
        }

        InventoryItem updated = inventoryItemRepository.save(item);
        logger.info("Inventory item updated: id={}, userId={}", updated.getId(), userId);
        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public InventoryItemResponseDTO archiveItem(Long userId, Long itemId) {
        logger.debug("Archiving inventory item: userId={}, itemId={}", userId, itemId);

        InventoryItem item = inventoryItemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> {
                logger.warn("Item not found for archive: userId={}, itemId={}", userId, itemId);
                return new InventoryItemNotFoundException("Item not found");
            });

        item.setStatus(ItemStatus.ARCHIVED);
        InventoryItem updated = inventoryItemRepository.save(item);
        logger.info("Inventory item archived: id={}, userId={}", updated.getId(), userId);
        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public InventoryItemResponseDTO restoreItem(Long userId, Long itemId) {
        logger.debug("Restoring inventory item: userId={}, itemId={}", userId, itemId);

        InventoryItem item = inventoryItemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> {
                logger.warn("Item not found for restore: userId={}, itemId={}", userId, itemId);
                return new InventoryItemNotFoundException("Item not found");
            });

        item.setStatus(ItemStatus.ACTIVE);
        InventoryItem updated = inventoryItemRepository.save(item);
        logger.info("Inventory item restored: id={}, userId={}", updated.getId(), userId);
        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        logger.debug("Deleting inventory item: userId={}, itemId={}", userId, itemId);

        InventoryItem item = inventoryItemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> {
                logger.warn("Item not found for delete: userId={}, itemId={}", userId, itemId);
                return new InventoryItemNotFoundException("Item not found");
            });

        inventoryItemRepository.deleteByIdAndUserId(itemId, userId);
        logger.warn("Inventory item deleted: id={}, userId={}", itemId, userId);
    }

    private void validateCategory(Long categoryId, Long userId) {
        if (!categoryRepository.findByIdAndUserId(categoryId, userId).isPresent()) {
            logger.warn("Category not found or doesn't belong to user: categoryId={}, userId={}", categoryId, userId);
            throw new CategoryNotFoundException("Category not found");
        }
    }

    private void validateLocation(Long locationId, Long userId) {
        if (!locationRepository.findByIdAndUserId(locationId, userId).isPresent()) {
            logger.warn("Location not found or doesn't belong to user: locationId={}, userId={}", locationId, userId);
            throw new LocationNotFoundException("Location not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryItemResponseDTO> searchItems(Long userId, String searchTerm, int page, int size) {
        logger.debug("Searching inventory items: userId={}, searchTerm={}, page={}, size={}", userId, searchTerm, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<InventoryItem> results = inventoryItemRepository.searchByMultipleFields(userId, searchTerm, pageable);
        return results.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryItemResponseDTO> filterItems(Long userId, Long categoryId, Long locationId,
                                                       String status, String stockState, int page, int size) {
        logger.debug("Filtering inventory items: userId={}, categoryId={}, locationId={}, status={}, stockState={}, page={}, size={}",
            userId, categoryId, locationId, status, stockState, page, size);
        Pageable pageable = PageRequest.of(page, size);

        ItemStatus itemStatus = null;
        if (status != null && !status.equalsIgnoreCase("ALL")) {
            try {
                itemStatus = ItemStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid status provided: {}", status);
                itemStatus = null;
            }
        }

        Page<InventoryItem> results = inventoryItemRepository.filterByCategoryAndLocation(userId, categoryId, locationId, pageable);

        if (itemStatus != null) {
            results = results.map(item -> {
                if (item.getStatus() == itemStatus) {
                    return item;
                }
                return null;
            }).filter(item -> item != null);
        }

        if (stockState != null && !stockState.equalsIgnoreCase("ALL")) {
            results = applyStockStateFilter(results, stockState);
        }

        return results.map(this::convertToDTO);
    }

    private Page<InventoryItem> applyStockStateFilter(Page<InventoryItem> items, String stockState) {
        return items.map(item -> {
            boolean matches = false;
            if ("OUT_OF_STOCK".equalsIgnoreCase(stockState)) {
                matches = item.getCurrentQuantity().signum() == 0;
            } else if ("LOW_STOCK".equalsIgnoreCase(stockState)) {
                matches = item.getCurrentQuantity().signum() > 0 &&
                    item.getCurrentQuantity().compareTo(item.getLowStockThreshold()) <= 0;
            } else if ("IN_STOCK".equalsIgnoreCase(stockState)) {
                matches = item.getCurrentQuantity().compareTo(item.getLowStockThreshold()) > 0;
            }
            return matches ? item : null;
        }).filter(item -> item != null);
    }

    private InventoryItemResponseDTO convertToDTO(InventoryItem item) {
        return new InventoryItemResponseDTO(
            item.getId(),
            item.getName(),
            item.getDescription(),
            item.getSku(),
            item.getCategoryId(),
            item.getLocationId(),
            item.getCurrentQuantity(),
            item.getUnit(),
            item.getLowStockThreshold(),
            item.getStatus().toString(),
            item.getCreatedDate(),
            item.getUpdatedDate()
        );
    }
}
