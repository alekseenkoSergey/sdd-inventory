package org.example.sddinventory.repository;

import org.example.sddinventory.entity.InventoryItem;
import org.example.sddinventory.entity.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    @Query("SELECT i FROM InventoryItem i WHERE i.id = ?1 AND i.userId = ?2")
    Optional<InventoryItem> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT i FROM InventoryItem i WHERE i.userId = ?1 AND i.status = ?2 ORDER BY i.createdDate DESC")
    Page<InventoryItem> findByUserIdAndStatus(Long userId, ItemStatus status, Pageable pageable);

    @Query("SELECT i FROM InventoryItem i WHERE i.userId = ?1 ORDER BY i.createdDate DESC")
    Page<InventoryItem> findByUserId(Long userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM InventoryItem i WHERE i.id = ?1 AND i.userId = ?2")
    void deleteByIdAndUserId(Long id, Long userId);

    @Query("SELECT i FROM InventoryItem i WHERE i.userId = ?1 AND i.sku = ?2")
    Optional<InventoryItem> findByUserIdAndSku(Long userId, String sku);

    @Query("SELECT i FROM InventoryItem i WHERE i.userId = ?1 " +
           "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', ?2, '%')) " +
           "OR LOWER(i.description) LIKE LOWER(CONCAT('%', ?2, '%')) " +
           "OR LOWER(i.sku) LIKE LOWER(CONCAT('%', ?2, '%'))) " +
           "ORDER BY i.createdDate DESC")
    Page<InventoryItem> searchByMultipleFields(Long userId, String searchTerm, Pageable pageable);

    @Query("SELECT i FROM InventoryItem i WHERE i.userId = ?1 " +
           "AND (?2 IS NULL OR i.category.id = ?2) " +
           "AND (?3 IS NULL OR i.location.id = ?3) " +
           "ORDER BY i.createdDate DESC")
    Page<InventoryItem> filterByCategoryAndLocation(Long userId, Long categoryId, Long locationId, Pageable pageable);

    @Query("SELECT i FROM InventoryItem i WHERE i.userId = ?1 " +
           "AND (?2 IS NULL OR i.status = ?2) " +
           "ORDER BY i.createdDate DESC")
    Page<InventoryItem> filterByStatus(Long userId, ItemStatus status, Pageable pageable);
}
