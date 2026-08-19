package org.example.sddinventory.controller;

import org.example.sddinventory.model.UserProfileResponseDTO;
import org.example.sddinventory.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger("auth");
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public ResponseEntity<?> login() {
        // This endpoint is not directly called by clients. OAuth2 login is initiated via
        // /oauth2/authorization/google which is handled by Spring Security automatically.
        // Return 400 if this endpoint is called directly.
        return ResponseEntity.badRequest().body(Map.of("message", "Use /oauth2/authorization/google to login"));
    }

    @GetMapping("/error")
    public ResponseEntity<?> oauthError(String error, String error_description) {
        logger.warn("OAuth error callback: error={}, description={}, timestamp={}",
            error, error_description, System.currentTimeMillis());
        Map<String, String> response = new HashMap<>();
        response.put("error", error != null ? error : "unknown_error");
        response.put("message", error_description != null ? error_description : "Authentication failed. Please try again.");
        return ResponseEntity.status(400).body(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(Authentication authentication) {
        try {
            authService.logoutUser();
            Map<String, String> response = new HashMap<>();
            response.put("status", "logged_out");
            logger.info("Logout successful at timestamp={}", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.warn("Logout error: error={}, timestamp={}", e.getMessage(), System.currentTimeMillis());
            return ResponseEntity.status(500).body(Map.of("error", "Logout failed"));
        }
    }

    @GetMapping("/user/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String providerUserId = oAuth2User.getAttribute("sub");
        String provider = "Google";

        Long userId = authService.getUserIdFromAuthentication(provider, providerUserId);
        if (userId == null) {
            logger.warn("User not found in profile endpoint: provider={}, providerUserId={}, timestamp={}",
                provider, providerUserId, System.currentTimeMillis());
            return ResponseEntity.status(401).body(Map.of("error", "User not found"));
        }

        UserProfileResponseDTO profile = authService.getUserProfile(userId);
        if (profile == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Profile not found"));
        }

        return ResponseEntity.ok(profile);
    }
}
