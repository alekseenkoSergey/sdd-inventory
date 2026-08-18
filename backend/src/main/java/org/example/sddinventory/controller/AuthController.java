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
        return ResponseEntity.ok().body(Map.of("message", "Redirect to OAuth2 login"));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(Authentication authentication) {
        authService.logoutUser();
        Map<String, String> response = new HashMap<>();
        response.put("status", "logged_out");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String providerUserId = oAuth2User.getAttribute("sub");
        String provider = "google";

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
