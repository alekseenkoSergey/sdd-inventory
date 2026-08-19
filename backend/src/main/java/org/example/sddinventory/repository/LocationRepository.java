package org.example.sddinventory.repository;

import org.example.sddinventory.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByUserId(Long userId);

    Optional<Location> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndName(Long userId, String name);
}
