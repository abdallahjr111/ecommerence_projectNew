package com.example.ecommerence_project.util;

import com.example.ecommerence_project.entity.User;
import com.example.ecommerence_project.exception.UnauthorizedException;
import com.example.ecommerence_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserUtil {

    private final UserRepository userRepository;

    /**
     * Returns the currently authenticated user's email (subject).
     * Throws UnauthorizedException if no authentication is present.
     */
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new UnauthorizedException("No authenticated user found");
        }
        return auth.getName();
    }

    /**
     * Returns the full User entity for the currently authenticated user.
     */
    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found in database"));
    }
}
