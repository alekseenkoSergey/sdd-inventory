package org.example.sddinventory.controller;

import org.example.sddinventory.entity.ItemStatus;
import org.example.sddinventory.entity.User;
import org.example.sddinventory.model.InventoryItemPatchDTO;
import org.example.sddinventory.model.InventoryItemRequestDTO;
import org.example.sddinventory.model.InventoryItemResponseDTO;
import org.example.sddinventory.service.AuthService;
import org.example.sddinventory.service.InventoryItemService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/inventory-items")
@Validated
public class InventoryItemController {
    private final InventoryItemService inventoryItemService;
    private final AuthService authService;

    public InventoryItemController(InventoryItemService inventoryItemService, AuthService authService) {
        this.inventoryItemService = inventoryItemService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<InventoryItemResponseDTO> createItem(
        @Valid @RequestBody InventoryItemRequestDTO request,
        Authentication authentication) {
        Long userId = extractUserId(authentication);
        InventoryItemResponseDTO item = inventoryItemService.createItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<InventoryItemResponseDTO> getItem(
        @PathVariable Long itemId,
        Authentication authentication) {
        Long userId = extractUserId(authentication);
        InventoryItemResponseDTO item = inventoryItemService.getItem(userId, itemId);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<Page<InventoryItemResponseDTO>> listItems(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) ItemStatus status,
        Authentication authentication) {
        Long userId = extractUserId(authentication);
        Page<InventoryItemResponseDTO> items = inventoryItemService.listItems(userId, page, size, status);
        return ResponseEntity.ok(items);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<InventoryItemResponseDTO> updateItem(
        @PathVariable Long itemId,
        @Valid @RequestBody InventoryItemPatchDTO request,
        Authentication authentication) {
        Long userId = extractUserId(authentication);
        InventoryItemResponseDTO item = inventoryItemService.updateItem(userId, itemId, request);
        return ResponseEntity.ok(item);
    }

    @PostMapping("/{itemId}/archive")
    public ResponseEntity<InventoryItemResponseDTO> archiveItem(
        @PathVariable Long itemId,
        Authentication authentication) {
        Long userId = extractUserId(authentication);
        InventoryItemResponseDTO item = inventoryItemService.archiveItem(userId, itemId);
        return ResponseEntity.ok(item);
    }

    @PostMapping("/{itemId}/restore")
    public ResponseEntity<InventoryItemResponseDTO> restoreItem(
        @PathVariable Long itemId,
        Authentication authentication) {
        Long userId = extractUserId(authentication);
        InventoryItemResponseDTO item = inventoryItemService.restoreItem(userId, itemId);
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
        @PathVariable Long itemId,
        Authentication authentication) {
        Long userId = extractUserId(authentication);
        inventoryItemService.deleteItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(Authentication authentication) {
        User currentUser = authService.getCurrentUser();
        return currentUser.getId();
    }
}
