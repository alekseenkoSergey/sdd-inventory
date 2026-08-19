package org.example.sddinventory.service;

import org.example.sddinventory.entity.Category;
import org.example.sddinventory.exception.CategoryNameNotUniqueException;
import org.example.sddinventory.model.CategoryResponseDTO;
import org.example.sddinventory.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceEdgeCasesTest {
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ItemService itemService;

    private Long userId;

    @BeforeEach
    public void setUp() {
        categoryService = new CategoryService(categoryRepository, itemService);
        userId = 1L;
    }

    @Test
    public void testEmptyNameAfterTrimming() {
        lenient().when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, ""))
            .thenReturn(Optional.empty());

        Category category = new Category(userId, "");
        category.setId(1L);
        lenient().when(categoryRepository.save(any())).thenReturn(category);
        lenient().when(itemService.countItemsByCategory(1L)).thenReturn(0);

        CategoryResponseDTO result = categoryService.createCategory(userId, "   ");
        assertEquals("", result.getName());
    }

    @Test
    public void testVeryLongCategoryName() {
        String longName = "A".repeat(255);
        lenient().when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, longName))
            .thenReturn(Optional.empty());

        Category category = new Category(userId, longName);
        category.setId(1L);
        lenient().when(categoryRepository.save(any())).thenReturn(category);
        lenient().when(itemService.countItemsByCategory(1L)).thenReturn(0);

        CategoryResponseDTO result = categoryService.createCategory(userId, longName);
        assertEquals(255, result.getName().length());
    }

    @Test
    public void testSpecialCharactersInName() {
        String specialName = "Electronics & Tools (2024)";
        lenient().when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, specialName))
            .thenReturn(Optional.empty());

        Category category = new Category(userId, specialName);
        category.setId(1L);
        lenient().when(categoryRepository.save(any())).thenReturn(category);
        lenient().when(itemService.countItemsByCategory(1L)).thenReturn(0);

        CategoryResponseDTO result = categoryService.createCategory(userId, specialName);
        assertEquals(specialName, result.getName());
    }

    @Test
    public void testCaseInsensitiveUniqueness() {
        Long categoryId = 1L;
        Category existing = new Category(userId, "ELECTRONICS");
        existing.setId(categoryId);

        when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, "electronics"))
            .thenReturn(Optional.of(existing));

        assertThrows(CategoryNameNotUniqueException.class,
            () -> categoryService.createCategory(userId, "electronics"));
    }

    @Test
    public void testMixedCaseAndWhitespace() {
        lenient().when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, "ElEcTrOnIcS"))
            .thenReturn(Optional.empty());

        Category category = new Category(userId, "ElEcTrOnIcS");
        category.setId(1L);
        lenient().when(categoryRepository.save(any())).thenReturn(category);
        lenient().when(itemService.countItemsByCategory(1L)).thenReturn(0);

        categoryService.createCategory(userId, "  ElEcTrOnIcS  ");

        verify(categoryRepository).save(argThat(c -> "ElEcTrOnIcS".equals(c.getName())));
    }
}
