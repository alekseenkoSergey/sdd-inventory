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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceDeleteTest {
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
    public void testDeletionWithItemsCheck() {
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
    public void testSuccessfulEmptyCategoryDeletion() {
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
    public void testDeleteNonExistentCategory() {
        UUID categoryId = UUID.randomUUID();

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
            .thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
            () -> categoryService.deleteCategory(userId, categoryId));
    }

    @Test
    public void testDeleteWithVersionConflict() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(userId, "Electronics");
        category.setId(categoryId);
        category.setVersion(2L);

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
            .thenReturn(Optional.of(category));
        when(itemService.countItemsByCategory(categoryId)).thenReturn(0);
        when(categoryRepository.deleteByIdAndUserId(categoryId, userId))
            .thenThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException("Version conflict", null));

        assertThrows(org.springframework.orm.ObjectOptimisticLockingFailureException.class,
            () -> categoryService.deleteCategory(userId, categoryId));
    }
}
