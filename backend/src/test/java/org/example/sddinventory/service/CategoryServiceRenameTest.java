package org.example.sddinventory.service;

import org.example.sddinventory.entity.Category;
import org.example.sddinventory.exception.CategoryNameNotUniqueException;
import org.example.sddinventory.exception.CategoryNotFoundException;
import org.example.sddinventory.model.CategoryResponseDTO;
import org.example.sddinventory.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceRenameTest {
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
    public void testRenameCategoryWithUniquenesCheck() {
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
    }

    @Test
    public void testVersionConflictOnRename() {
        UUID categoryId = UUID.randomUUID();
        Category existing = new Category(userId, "Electronics");
        existing.setId(categoryId);
        existing.setVersion(1L);

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
            .thenReturn(Optional.of(existing));
        when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, "Tools"))
            .thenReturn(Optional.empty());
        when(categoryRepository.save(any()))
            .thenThrow(new ObjectOptimisticLockingFailureException("Version conflict", null));

        assertThrows(ObjectOptimisticLockingFailureException.class,
            () -> categoryService.renameCategory(userId, categoryId, "Tools"));
    }

    @Test
    public void testRenameToDuplicateName() {
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
    public void testRenameConcurrentEdit() {
        UUID categoryId = UUID.randomUUID();
        Category existing = new Category(userId, "Electronics");
        existing.setId(categoryId);

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
            .thenReturn(Optional.of(existing));
        when(categoryRepository.findByUserIdAndNameIgnoreCase(userId, "Tools"))
            .thenReturn(Optional.empty());
        when(categoryRepository.save(any()))
            .thenThrow(new ObjectOptimisticLockingFailureException("Concurrent edit", null));

        assertThrows(ObjectOptimisticLockingFailureException.class,
            () -> categoryService.renameCategory(userId, categoryId, "Tools"));
    }
}
