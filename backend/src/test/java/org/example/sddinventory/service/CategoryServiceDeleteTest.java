package org.example.sddinventory.service;

import org.example.sddinventory.entity.Category;
import org.example.sddinventory.exception.CategoryHasItemsException;
import org.example.sddinventory.exception.CategoryNotFoundException;
import org.example.sddinventory.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceDeleteTest {
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
    public void testDeletionWithItemsCheck() {
        Long categoryId = 1L;
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
    public void testSuccessfulEmptyCategoryDeletion() {
        Long categoryId = 1L;
        Category category = new Category(userId, "Electronics");
        category.setId(categoryId);

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
            .thenReturn(Optional.of(category));
        when(itemService.countItemsByCategory(categoryId)).thenReturn(0);

        categoryService.deleteCategory(userId, categoryId);

        verify(categoryRepository, times(1)).deleteByIdAndUserId(categoryId, userId);
    }

    @Test
    public void testDeleteNonExistentCategory() {
        Long categoryId = 1L;

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
            .thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
            () -> categoryService.deleteCategory(userId, categoryId));
    }

    @Test
    public void testDeleteWithVersionConflict() {
        Long categoryId = 1L;
        Category category = new Category(userId, "Electronics");
        category.setId(categoryId);
        category.setVersion(2L);

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
            .thenReturn(Optional.of(category));
        when(itemService.countItemsByCategory(categoryId)).thenReturn(0);
        doThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException("Version conflict", null))
            .when(categoryRepository).deleteByIdAndUserId(categoryId, userId);

        assertThrows(org.springframework.orm.ObjectOptimisticLockingFailureException.class,
            () -> categoryService.deleteCategory(userId, categoryId));
    }
}
