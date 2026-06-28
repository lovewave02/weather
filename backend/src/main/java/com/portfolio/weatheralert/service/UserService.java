package com.portfolio.weatheralert.service;

import java.util.UUID;

import com.portfolio.weatheralert.domain.AppUser;
import com.portfolio.weatheralert.repository.AppUserRepository;
import com.portfolio.weatheralert.service.dto.CreateUserRequest;
import com.portfolio.weatheralert.service.dto.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final AppUserRepository appUserRepository;

    public UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        AppUser saved = appUserRepository.save(new AppUser(request.email()));
        return UserResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("user not found for email: " + email));
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("user not found: " + userId));
        return UserResponse.from(user);
    }
}
