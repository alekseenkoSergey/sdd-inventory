package org.example.sddinventory.service;

import org.example.sddinventory.entity.Category;
import org.example.sddinventory.exception.CategoryHasItemsException;
import org.example.sddinventory.exception.CategoryNameNotUniqueException;
import org.example.sddinventory.exception.CategoryNotFoundException;
import org.example.sddinventory.model.CategoryResponseDTO;
import org.example.sddinventory.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryRepository categoryRepository;
    private final ItemService itemService;

    public CategoryService(CategoryRepository categoryRepository, ItemService itemService) {
        this.categoryRepository = categoryRepository;
        this.itemService = itemService;
    }

    @Transactional
    public CategoryResponseDTO createCategory(Long userId, String name) {
        String trimmedName = name.trim();
        logger.debug("Creating category: userId={}, name={}", userId, trimmedName);

        if (categoryRepository.findByUserIdAndNameIgnoreCase(userId, trimmedName).isPresent()) {
            logger.warn("Attempt to create duplicate category: userId={}, name={}", userId, trimmedName);
            throw new CategoryNameNotUniqueException("Category name already exists");
        }

        Category category = new Category(userId, trimmedName);
        Category saved = categoryRepository.save(category);

        logger.info("Category created successfully: id={}, userId={}, name={}", saved.getId(), userId, trimmedName);
        return convertToDTO(saved, 0);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> listCategories(Long userId) {
        return categoryRepository.findAllByUserId(userId).stream()
            .map(cat -> {
                int itemCount = itemService.countItemsByCategory(cat.getId());
                return convertToDTO(cat, itemCount);
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponseDTO renameCategory(Long userId, Long categoryId, String newName) {
        String trimmedName = newName.trim();

        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(() -> {
                logger.warn("Attempt to rename non-existent category: userId={}, categoryId={}", userId, categoryId);
                return new CategoryNotFoundException("Category not found");
            });

        if (categoryRepository.findByUserIdAndNameIgnoreCase(userId, trimmedName).isPresent() &&
            !category.getName().equalsIgnoreCase(trimmedName)) {
            logger.warn("Attempt to rename to duplicate name: userId={}, categoryId={}, newName={}", userId, categoryId, trimmedName);
            throw new CategoryNameNotUniqueException("Category name already exists");
        }

        category.setName(trimmedName);
        category.setUpdatedAt(LocalDateTime.now());
        Category updated = categoryRepository.save(category);

        logger.info("Category renamed: id={}, userId={}, newName={}", categoryId, userId, trimmedName);
        int itemCount = itemService.countItemsByCategory(categoryId);
        return convertToDTO(updated, itemCount);
    }

    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        logger.debug("Deleting category: userId={}, categoryId={}", userId, categoryId);

        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(() -> {
                logger.warn("Attempt to delete non-existent category: userId={}, categoryId={}", userId, categoryId);
                return new CategoryNotFoundException("Category not found");
            });

        int itemCount = itemService.countItemsByCategory(categoryId);
        if (itemCount > 0) {
            logger.warn("Attempt to delete category with items: userId={}, categoryId={}, itemCount={}", userId, categoryId, itemCount);
            throw new CategoryHasItemsException("Cannot delete: " + itemCount + " items assigned. Please reassign items to another category first.", itemCount);
        }

        categoryRepository.deleteByIdAndUserId(categoryId, userId);
        logger.info("Category deleted successfully: id={}, userId={}", categoryId, userId);
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(() -> {
                logger.warn("Attempt to retrieve non-existent category: userId={}, categoryId={}", userId, categoryId);
                return new CategoryNotFoundException("Category not found");
            });

        int itemCount = itemService.countItemsByCategory(categoryId);
        return convertToDTO(category, itemCount);
    }

    private CategoryResponseDTO convertToDTO(Category category, int itemCount) {
        return new CategoryResponseDTO(
            category.getId(),
            category.getName(),
            itemCount,
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}
