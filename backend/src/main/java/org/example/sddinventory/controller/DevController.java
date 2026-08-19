package org.example.sddinventory.controller;

import org.example.sddinventory.model.CategoryResponseDTO;
import org.example.sddinventory.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dev")
public class DevController {
    private final CategoryService categoryService;

    public DevController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories/{userId}")
    public ResponseEntity<List<CategoryResponseDTO>> getCategories(@PathVariable Long userId) {
        List<CategoryResponseDTO> categories = categoryService.listCategories(userId);
        return ResponseEntity.ok(categories);
    }
}
