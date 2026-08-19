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
import java.util.UUID;

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

    private UUID userId;

    @BeforeEach
    public void setUp() {
        categoryService = new CategoryService(categoryRepository, itemService);
        userId = UUID.randomUUID();
    }

    @Test
    public void testEmptyNameAfterTrimming() {
        when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, ""))
            .thenReturn(Optional.empty());

        Category category = new Category(userId, "");
        category.setId(UUID.randomUUID());
        when(categoryRepository.save(any())).thenReturn(category);
        when(itemService.countItemsByCategory(category.getId())).thenReturn(0);

        CategoryResponseDTO result = categoryService.createCategory(userId, "   ");
        assertEquals("", result.getName());
    }

    @Test
    public void testVeryLongCategoryName() {
        String longName = "A".repeat(255);
        when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, longName))
            .thenReturn(Optional.empty());

        Category category = new Category(userId, longName);
        category.setId(UUID.randomUUID());
        when(categoryRepository.save(any())).thenReturn(category);
        when(itemService.countItemsByCategory(category.getId())).thenReturn(0);

        CategoryResponseDTO result = categoryService.createCategory(userId, longName);
        assertEquals(255, result.getName().length());
    }

    @Test
    public void testSpecialCharactersInName() {
        String specialName = "Electronics & Tools (2024)";
        when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, specialName))
            .thenReturn(Optional.empty());

        Category category = new Category(userId, specialName);
        category.setId(UUID.randomUUID());
        when(categoryRepository.save(any())).thenReturn(category);
        when(itemService.countItemsByCategory(category.getId())).thenReturn(0);

        CategoryResponseDTO result = categoryService.createCategory(userId, specialName);
        assertEquals(specialName, result.getName());
    }

    @Test
    public void testCaseInsensitiveUniqueness() {
        UUID categoryId = UUID.randomUUID();
        Category existing = new Category(userId, "ELECTRONICS");
        existing.setId(categoryId);

        when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, "electronics"))
            .thenReturn(Optional.of(existing));

        assertThrows(CategoryNameNotUniqueException.class,
            () -> categoryService.createCategory(userId, "electronics"));
    }

    @Test
    public void testMixedCaseAndWhitespace() {
        when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, "Electronics"))
            .thenReturn(Optional.empty());

        Category category = new Category(userId, "Electronics");
        category.setId(UUID.randomUUID());
        when(categoryRepository.save(any())).thenReturn(category);
        when(itemService.countItemsByCategory(category.getId())).thenReturn(0);

        categoryService.createCategory(userId, "  ElEcTrOnIcS  ");

        verify(categoryRepository).save(argThat(c -> "ElEcTrOnIcS".equals(c.getName())));
    }
}
