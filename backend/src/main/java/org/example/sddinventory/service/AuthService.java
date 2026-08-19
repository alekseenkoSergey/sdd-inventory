package org.example.sddinventory.service;

import org.example.sddinventory.entity.User;
import org.example.sddinventory.model.UserProfileResponseDTO;
import org.example.sddinventory.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger("auth");
    public final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
                org.springframework.security.oauth2.core.user.OAuth2User oAuth2User =
                    (org.springframework.security.oauth2.core.user.OAuth2User) principal;
                String providerUserId = oAuth2User.getAttribute("sub");
                String provider = "Google";

                Optional<User> user = userRepository.findByProviderAndProviderUserId(provider, providerUserId);
                return user.orElse(null);
            }
        }
        return null;
    }

    public void logoutUser() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                logger.info("User logout: userId={}, timestamp={}", currentUser.getId(), System.currentTimeMillis());
            }

            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr != null) {
                HttpServletRequest request = attr.getRequest();
                HttpServletResponse response = attr.getResponse();
                if (response != null) {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null) {
                        new SecurityContextLogoutHandler().logout(request, response, authentication);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error during logout: error={}, timestamp={}", e.getMessage(), System.currentTimeMillis());
        }
    }

    public UserProfileResponseDTO getUserProfile(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            logger.warn("User not found: userId={}, timestamp={}", userId, System.currentTimeMillis());
            return null;
        }

        User user = userOpt.get();
        logger.info("Profile retrieved: userId={}, timestamp={}", userId, System.currentTimeMillis());

        return new UserProfileResponseDTO(
            user.getId(),
            user.getProvider(),
            user.getEmail(),
            user.getDisplayName(),
            user.getAvatarUrl()
        );
    }

    public void verifyUserExists(String provider, String providerUserId) {
        Optional<User> user = userRepository.findByProviderAndProviderUserId(provider, providerUserId);
        if (user.isPresent()) {
            logger.info("User exists: provider={}, providerUserId={}, userId={}, timestamp={}",
                provider, providerUserId, user.get().getId(), System.currentTimeMillis());
        }
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
               !"anonymousUser".equals(authentication.getPrincipal());
    }

    public Long getUserIdFromAuthentication(String provider, String providerUserId) {
        Optional<User> user = userRepository.findByProviderAndProviderUserId(provider, providerUserId);
        return user.map(User::getId).orElse(null);
    }

    public User createOrGetUser(String provider, String providerUserId, String email, String displayName, String avatarUrl) {
        Optional<User> existingUser = userRepository.findByProviderAndProviderUserId(provider, providerUserId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            boolean updated = false;

            if (email != null && !email.equals(user.getEmail())) {
                user.setEmail(email);
                updated = true;
            }
            if (displayName != null && !displayName.equals(user.getDisplayName())) {
                user.setDisplayName(displayName);
                updated = true;
            }
            if (avatarUrl != null && !avatarUrl.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(avatarUrl);
                updated = true;
            }

            if (updated) {
                user = userRepository.save(user);
                logger.info("User updated: userId={}, email={}, provider={}, timestamp={}",
                    user.getId(), email, provider, System.currentTimeMillis());
            }
            return user;
        }

        User newUser = new User(provider, providerUserId, email, displayName, avatarUrl);
        newUser = userRepository.save(newUser);
        logger.info("New user created: userId={}, email={}, provider={}, providerUserId={}, timestamp={}",
            newUser.getId(), email, provider, providerUserId, System.currentTimeMillis());
        return newUser;
    }
}
