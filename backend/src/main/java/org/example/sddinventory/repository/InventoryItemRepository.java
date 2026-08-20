package org.example.sddinventory.repository;

import org.example.sddinventory.entity.InventoryItem;
import org.example.sddinventory.entity.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    @Query("SELECT i FROM InventoryItem i WHERE i.id = ?1 AND i.userId = ?2")
    Optional<InventoryItem> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT i FROM InventoryItem i WHERE i.userId = ?1 AND i.status = ?2 ORDER BY i.createdDate DESC")
    Page<InventoryItem> findByUserIdAndStatus(Long userId, ItemStatus status, Pageable pageable);

    @Query("SELECT i FROM InventoryItem i WHERE i.userId = ?1 ORDER BY i.createdDate DESC")
    Page<InventoryItem> findByUserId(Long userId, Pageable pageable);

    @Query("DELETE FROM InventoryItem i WHERE i.id = ?1 AND i.userId = ?2")
    void deleteByIdAndUserId(Long id, Long userId);

    @Query("SELECT i FROM InventoryItem i WHERE i.userId = ?1 AND i.sku = ?2")
    Optional<InventoryItem> findByUserIdAndSku(Long userId, String sku);
}
