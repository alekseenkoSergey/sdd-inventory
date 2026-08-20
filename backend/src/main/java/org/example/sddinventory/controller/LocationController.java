package org.example.sddinventory.controller;

import org.example.sddinventory.entity.User;
import org.example.sddinventory.model.CreateLocationRequestDTO;
import org.example.sddinventory.model.LocationResponseDTO;
import org.example.sddinventory.model.RenameLocationRequestDTO;
import org.example.sddinventory.service.AuthService;
import org.example.sddinventory.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {
    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);
    private final LocationService locationService;
    private final AuthService authService;

    public LocationController(LocationService locationService, AuthService authService) {
        this.locationService = locationService;
        this.authService = authService;
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) principal;
            String providerUserId = oAuth2User.getAttribute("sub");
            String provider = "Google";

            User user = authService.createOrGetUser(
                provider,
                providerUserId,
                oAuth2User.getAttribute("email"),
                oAuth2User.getAttribute("name"),
                oAuth2User.getAttribute("picture")
            );

            if (user == null) {
                throw new IllegalArgumentException("Failed to authenticate user");
            }
            return user.getId();
        }

        throw new IllegalArgumentException("User not authenticated");
    }

    @PostMapping
    public ResponseEntity<LocationResponseDTO> createLocation(
            @Valid @RequestBody CreateLocationRequestDTO request,
            Authentication authentication) {
        logger.debug("POST /locations - Creating location: name={}", request.getName());
        Long userId = extractUserId(authentication);
        LocationResponseDTO response = locationService.createLocation(userId, request.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LocationResponseDTO>> getAllLocations(Authentication authentication) {
        logger.debug("GET /locations - Listing all locations");
        Long userId = extractUserId(authentication);
        List<LocationResponseDTO> response = locationService.getAllLocations(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponseDTO> getLocation(
            @PathVariable Long id,
            Authentication authentication) {
        logger.debug("GET /locations/{} - Fetching location", id);
        Long userId = extractUserId(authentication);
        LocationResponseDTO response = locationService.getLocation(userId, id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponseDTO> renameLocation(
            @PathVariable Long id,
            @Valid @RequestBody RenameLocationRequestDTO request,
            Authentication authentication) {
        logger.debug("PUT /locations/{} - Renaming location: newName={}", id, request.getName());
        Long userId = extractUserId(authentication);
        LocationResponseDTO response = locationService.renameLocation(userId, id, request.getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(
            @PathVariable Long id,
            Authentication authentication) {
        logger.debug("DELETE /locations/{} - Deleting location", id);
        Long userId = extractUserId(authentication);
        locationService.deleteLocation(userId, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
