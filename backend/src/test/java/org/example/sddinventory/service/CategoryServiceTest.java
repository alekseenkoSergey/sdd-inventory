package org.example.sddinventory.service;

import org.example.sddinventory.entity.Category;
import org.example.sddinventory.exception.CategoryHasItemsException;
import org.example.sddinventory.exception.CategoryNameNotUniqueException;
import org.example.sddinventory.exception.CategoryNotFoundException;
import org.example.sddinventory.model.CategoryResponseDTO;
import org.example.sddinventory.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
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
    public void testCreateCategorySuccess() {
        when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, "Electronics"))
            .thenReturn(Optional.empty());

        Category category = new Category(userId, "Electronics");
        category.setId(UUID.randomUUID());
        when(categoryRepository.save(any())).thenReturn(category);
        when(itemService.countItemsByCategory(category.getId())).thenReturn(0);

        CategoryResponseDTO result = categoryService.createCategory(userId, "Electronics");

        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        assertEquals(0, result.getItemCount());
        verify(categoryRepository, times(1)).save(any());
    }

    @Test
    public void testCreateCategoryNameAlreadyExists() {
        Category existing = new Category(userId, "Electronics");
        when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, "Electronics"))
            .thenReturn(Optional.of(existing));

        assertThrows(CategoryNameNotUniqueException.class,
            () -> categoryService.createCategory(userId, "Electronics"));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    public void testNameTrimming() {
        when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, "Electronics"))
            .thenReturn(Optional.empty());

        Category category = new Category(userId, "Electronics");
        category.setId(UUID.randomUUID());
        when(categoryRepository.save(any())).thenReturn(category);
        when(itemService.countItemsByCategory(category.getId())).thenReturn(0);

        categoryService.createCategory(userId, "  Electronics  ");

        verify(categoryRepository).save(argThat(c -> "Electronics".equals(c.getName())));
    }

    @Test
    public void testRenameCategorySuccess() {
        UUID categoryId = UUID.randomUUID();
        Category existing = new Category(userId, "Electronics");
        existing.setId(categoryId);

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
            .thenReturn(Optional.of(existing));
        when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, "Tools"))
            .thenReturn(Optional.empty());

        Category renamed = new Category(userId, "Tools");
        renamed.setId(categoryId);
        when(categoryRepository.save(any())).thenReturn(renamed);
        when(itemService.countItemsByCategory(categoryId)).thenReturn(0);

        CategoryResponseDTO result = categoryService.renameCategory(userId, categoryId, "Tools");

        assertNotNull(result);
        assertEquals("Tools", result.getName());
        verify(categoryRepository, times(1)).save(any());
    }

    @Test
    public void testRenameCategoryToExistingName() {
        UUID categoryId = UUID.randomUUID();
        Category existing = new Category(userId, "Electronics");
        existing.setId(categoryId);
        Category duplicate = new Category(userId, "Tools");

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
            .thenReturn(Optional.of(existing));
        when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, "Tools"))
            .thenReturn(Optional.of(duplicate));

        assertThrows(CategoryNameNotUniqueException.class,
            () -> categoryService.renameCategory(userId, categoryId, "Tools"));
    }

    @Test
    public void testDeleteCategorySuccess() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(userId, "Electronics");
        category.setId(categoryId);

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
            .thenReturn(Optional.of(category));
        when(itemService.countItemsByCategory(categoryId)).thenReturn(0);

        categoryService.deleteCategory(userId, categoryId);

        verify(categoryRepository, times(1)).deleteByIdAndUserId(categoryId, userId);
    }

    @Test
    public void testDeleteCategoryWithItemsBlocked() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(userId, "Electronics");
        category.setId(categoryId);

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
            .thenReturn(Optional.of(category));
        when(itemService.countItemsByCategory(categoryId)).thenReturn(5);

        CategoryHasItemsException ex = assertThrows(CategoryHasItemsException.class,
            () -> categoryService.deleteCategory(userId, categoryId));
        assertEquals(5, ex.getItemCount());
        verify(categoryRepository, never()).deleteByIdAndUserId(categoryId, userId);
    }

    @Test
    public void testUserIsolationEnforced() {
        UUID categoryId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        when(categoryRepository.findByIdAndUserId(categoryId, otherUserId))
            .thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
            () -> categoryService.getCategory(otherUserId, categoryId));
    }
}
