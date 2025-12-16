package com.portfolio.weatheralert.service;

import com.portfolio.weatheralert.domain.AppUser;
import com.portfolio.weatheralert.repository.AppUserRepository;
import com.portfolio.weatheralert.service.dto.CreateUserRequest;
import com.portfolio.weatheralert.service.dto.UserResponse;
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
}

