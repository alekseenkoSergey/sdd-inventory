package org.example.sddinventory.service;

import org.example.sddinventory.entity.ItemStatus;
import org.example.sddinventory.model.InventoryItemPatchDTO;
import org.example.sddinventory.model.InventoryItemRequestDTO;
import org.example.sddinventory.model.InventoryItemResponseDTO;
import org.springframework.data.domain.Page;

public interface InventoryItemService {
    InventoryItemResponseDTO createItem(Long userId, InventoryItemRequestDTO requestDTO);

    InventoryItemResponseDTO getItem(Long userId, Long itemId);

    Page<InventoryItemResponseDTO> listItems(Long userId, int page, int size, ItemStatus status);

    InventoryItemResponseDTO updateItem(Long userId, Long itemId, InventoryItemPatchDTO patchDTO);

    InventoryItemResponseDTO archiveItem(Long userId, Long itemId);

    InventoryItemResponseDTO restoreItem(Long userId, Long itemId);

    void deleteItem(Long userId, Long itemId);
}
