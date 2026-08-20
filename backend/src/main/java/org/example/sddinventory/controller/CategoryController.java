package org.example.sddinventory.controller;

import org.example.sddinventory.entity.User;
import org.example.sddinventory.model.CategoryResponseDTO;
import org.example.sddinventory.model.CreateCategoryRequestDTO;
import org.example.sddinventory.model.RenameCategoryRequestDTO;
import org.example.sddinventory.service.AuthService;
import org.example.sddinventory.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Validated
public class CategoryController {
    private final CategoryService categoryService;
    private final AuthService authService;

    public CategoryController(CategoryService categoryService, AuthService authService) {
        this.categoryService = categoryService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
        @Valid @RequestBody CreateCategoryRequestDTO request,
        Authentication authentication) {
        Long userId = extractUserId(authentication);
        CategoryResponseDTO category = categoryService.createCategory(userId, request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> listCategories(Authentication authentication) {
        Long userId = extractUserId(authentication);
        List<CategoryResponseDTO> categories = categoryService.listCategories(userId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> getCategory(
        @PathVariable Long categoryId,
        Authentication authentication) {
        Long userId = extractUserId(authentication);
        CategoryResponseDTO category = categoryService.getCategory(userId, categoryId);
        return ResponseEntity.ok(category);
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> renameCategory(
        @PathVariable Long categoryId,
        @Valid @RequestBody RenameCategoryRequestDTO request,
        Authentication authentication) {
        Long userId = extractUserId(authentication);
        CategoryResponseDTO category = categoryService.renameCategory(userId, categoryId, request.getName());
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
        @PathVariable Long categoryId,
        Authentication authentication) {
        Long userId = extractUserId(authentication);
        categoryService.deleteCategory(userId, categoryId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<?> handleOptimisticLockException(org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT).build();
    }

    private Long extractUserId(Authentication authentication) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalArgumentException("User not authenticated");
        }
        return currentUser.getId();
    }
}
