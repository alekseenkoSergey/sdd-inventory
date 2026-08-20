package org.example.sddinventory.repository;

import org.example.sddinventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByItemIdOrderByCreatedDateAsc(Long itemId);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.itemId = :itemId AND sm.movementDate >= :startDate AND sm.movementDate <= :endDate ORDER BY sm.createdDate ASC")
    List<StockMovement> findByItemIdAndDateRange(@Param("itemId") Long itemId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
}
