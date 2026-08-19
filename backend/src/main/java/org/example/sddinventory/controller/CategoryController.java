package org.example.sddinventory.controller;

import org.example.sddinventory.model.CategoryResponseDTO;
import org.example.sddinventory.model.CreateCategoryRequestDTO;
import org.example.sddinventory.model.RenameCategoryRequestDTO;
import org.example.sddinventory.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@Validated
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
        @Valid @RequestBody CreateCategoryRequestDTO request,
        Authentication authentication) {
        UUID userId = extractUserId(authentication);
        CategoryResponseDTO category = categoryService.createCategory(userId, request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> listCategories(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        List<CategoryResponseDTO> categories = categoryService.listCategories(userId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> getCategory(
        @PathVariable UUID categoryId,
        Authentication authentication) {
        UUID userId = extractUserId(authentication);
        CategoryResponseDTO category = categoryService.getCategory(userId, categoryId);
        return ResponseEntity.ok(category);
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> renameCategory(
        @PathVariable UUID categoryId,
        @Valid @RequestBody RenameCategoryRequestDTO request,
        Authentication authentication) {
        UUID userId = extractUserId(authentication);
        CategoryResponseDTO category = categoryService.renameCategory(userId, categoryId, request.getName());
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
        @PathVariable UUID categoryId,
        Authentication authentication) {
        UUID userId = extractUserId(authentication);
        categoryService.deleteCategory(userId, categoryId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<?> handleOptimisticLockException(org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT).build();
    }

    private UUID extractUserId(Authentication authentication) {
        // TODO: Extract actual userId from JWT token
        // For now, using a placeholder
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
}
