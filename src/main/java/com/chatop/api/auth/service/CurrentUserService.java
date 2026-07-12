package com.chatop.api.auth.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.user.model.User;
import com.chatop.api.user.repository.UserRepository;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Integer getUserId(Authentication authentication) {
        return Optional.ofNullable(authentication)
            .map(Authentication::getPrincipal)
            .filter(Jwt.class::isInstance)
            .map(Jwt.class::cast)
            .map(this::userIdFromToken)
            .orElseThrow(this::unauthorized);
    }

    @Transactional(readOnly = true)
    public User getUser(Authentication authentication) {
        return findUserById(getUserId(authentication));
    }

    @Transactional(readOnly = true)
    public User getUser(Integer userId) {
        return findUserById(userId);
    }

    private User findUserById(Integer userId) {
        return userRepository.findById(userId)
            .orElseThrow(this::unauthorized);
    }

    private Integer userIdFromToken(Jwt jwt) {
        Object userIdClaim = jwt.getClaims().get("userId");

        if (userIdClaim instanceof Number number) {
            return number.intValue();
        }

        if (userIdClaim instanceof String userId) {
            try {
                return Integer.valueOf(userId);
            } catch (NumberFormatException exception) {
                throw unauthorized();
            }
        }

        throw unauthorized();
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
}
