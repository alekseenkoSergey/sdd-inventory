package org.example.sddinventory.service;

import org.example.sddinventory.entity.Location;
import org.example.sddinventory.exception.LocationHasItemsException;
import org.example.sddinventory.exception.LocationNameNotUniqueException;
import org.example.sddinventory.exception.LocationNotFoundException;
import org.example.sddinventory.model.LocationResponseDTO;
import org.example.sddinventory.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LocationService {
    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);
    private final LocationRepository locationRepository;
    private final ItemService itemService;

    public LocationService(LocationRepository locationRepository, ItemService itemService) {
        this.locationRepository = locationRepository;
        this.itemService = itemService;
    }

    public LocationResponseDTO createLocation(Long userId, String name) {
        String trimmedName = name.trim();
        logger.debug("Creating location: userId={}, name={}", userId, trimmedName);

        if (locationRepository.existsByUserIdAndName(userId, trimmedName)) {
            logger.warn("Attempt to create duplicate location: userId={}, name={}", userId, trimmedName);
            throw new LocationNameNotUniqueException("A location with this name already exists in your account");
        }

        Location location = new Location(userId, trimmedName);
        Location saved = locationRepository.save(location);

        logger.info("Location created successfully: id={}, userId={}, name={}", saved.getId(), userId, trimmedName);
        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<LocationResponseDTO> getAllLocations(Long userId) {
        logger.debug("Fetching all locations for userId={}", userId);
        return locationRepository.findByUserId(userId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LocationResponseDTO getLocation(Long userId, Long locationId) {
        logger.debug("Fetching location: userId={}, locationId={}", userId, locationId);
        Location location = locationRepository.findByIdAndUserId(locationId, userId)
            .orElseThrow(() -> {
                logger.warn("Location not found: userId={}, locationId={}", userId, locationId);
                return new LocationNotFoundException("Location not found");
            });
        return convertToDTO(location);
    }

    public LocationResponseDTO renameLocation(Long userId, Long locationId, String newName) {
        String trimmedName = newName.trim();
        logger.debug("Renaming location: userId={}, locationId={}, newName={}", userId, locationId, trimmedName);

        Location location = locationRepository.findByIdAndUserId(locationId, userId)
            .orElseThrow(() -> {
                logger.warn("Location not found for rename: userId={}, locationId={}", userId, locationId);
                return new LocationNotFoundException("Location not found");
            });

        if (locationRepository.existsByUserIdAndName(userId, trimmedName) &&
            !location.getName().equals(trimmedName)) {
            logger.warn("Attempt to rename to duplicate name: userId={}, locationId={}, newName={}", userId, locationId, trimmedName);
            throw new LocationNameNotUniqueException("A location with this name already exists in your account");
        }

        location.setName(trimmedName);
        location.setUpdatedAt(LocalDateTime.now());
        Location saved = locationRepository.save(location);

        logger.info("Location renamed successfully: id={}, userId={}, newName={}", saved.getId(), userId, trimmedName);
        return convertToDTO(saved);
    }

    public void deleteLocation(Long userId, Long locationId) {
        logger.debug("Deleting location: userId={}, locationId={}", userId, locationId);

        Location location = locationRepository.findByIdAndUserId(locationId, userId)
            .orElseThrow(() -> {
                logger.warn("Location not found for delete: userId={}, locationId={}", userId, locationId);
                return new LocationNotFoundException("Location not found");
            });

        int itemCount = itemService.countItemsByLocation(locationId);
        if (itemCount > 0) {
            logger.warn("Cannot delete non-empty location: userId={}, locationId={}, itemCount={}", userId, locationId, itemCount);
            throw new LocationHasItemsException("Cannot delete location with items. Please remove or reassign items first.", itemCount);
        }

        locationRepository.delete(location);
        logger.info("Location deleted successfully: id={}, userId={}", locationId, userId);
    }

    private LocationResponseDTO convertToDTO(Location location) {
        return new LocationResponseDTO(
            location.getId(),
            location.getUserId(),
            location.getName(),
            location.getCreatedAt(),
            location.getUpdatedAt()
        );
    }
}
