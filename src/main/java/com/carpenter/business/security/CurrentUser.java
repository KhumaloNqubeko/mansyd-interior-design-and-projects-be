package com.carpenter.business.security;

import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import com.carpenter.business.user.UserRepository;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) { this.userRepository = userRepository; }

    public User require(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorisedOperationException("Authentication is required.");
        }
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new UnauthorisedOperationException("Authenticated user no longer exists."));
    }

    public void requireOwnership(UUID ownerId, Authentication authentication) {
        User user = require(authentication);
        if (!user.getId().equals(ownerId) && user.getRole() != Role.CARPENTER) {
            throw new UnauthorisedOperationException("You cannot access another customer's resource.");
        }
    }

    public User requireRole(Authentication authentication, Role role) {
        User user = require(authentication);
        if (user.getRole() != role) {
            throw new UnauthorisedOperationException("You do not have permission to perform this action.");
        }
        return user;
    }
}
