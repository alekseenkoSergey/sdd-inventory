package org.example.sddinventory.service;

import org.example.sddinventory.entity.User;
import org.example.sddinventory.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private static final Logger logger = LoggerFactory.getLogger("auth");
    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerUserId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String displayName = oAuth2User.getAttribute("name");
        String avatarUrl = oAuth2User.getAttribute("picture");

        Optional<User> existingUser = userRepository.findByProviderAndProviderUserId(provider, providerUserId);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setDisplayName(displayName);
            user.setEmail(email);
            user.setAvatarUrl(avatarUrl);
            user = userRepository.save(user);
            logger.info("Existing user reused: provider={}, providerUserId={}, userId={}, timestamp={}",
                provider, providerUserId, user.getId(), System.currentTimeMillis());
        } else {
            user = new User(provider, providerUserId, email, displayName, avatarUrl);
            user = userRepository.save(user);
            logger.info("New user created: provider={}, providerUserId={}, email={}, timestamp={}",
                provider, providerUserId, email, System.currentTimeMillis());
        }

        return oAuth2User;
    }
}
