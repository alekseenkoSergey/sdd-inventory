package org.example.sddinventory.service;

import org.example.sddinventory.repository.InventoryItemRepository;
import org.springframework.stereotype.Service;

@Service
public class ItemCountingService {
    private final InventoryItemRepository inventoryItemRepository;

    public ItemCountingService(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    public int countItemsByCategory(Long categoryId) {
        return (int) inventoryItemRepository.findAll().stream()
                .filter(item -> item.getCategoryId().equals(categoryId))
                .count();
    }

    public int countItemsByLocation(Long locationId) {
        return (int) inventoryItemRepository.findAll().stream()
                .filter(item -> item.getLocationId().equals(locationId))
                .count();
    }
}
