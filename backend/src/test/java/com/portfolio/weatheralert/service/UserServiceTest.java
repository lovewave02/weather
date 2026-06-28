package com.portfolio.weatheralert.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.portfolio.weatheralert.domain.AppUser;
import com.portfolio.weatheralert.repository.AppUserRepository;
import com.portfolio.weatheralert.service.dto.CreateUserRequest;
import com.portfolio.weatheralert.service.dto.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UserServiceTest {

    private final AppUserRepository appUserRepository = Mockito.mock(AppUserRepository.class);
    private final UserService service = new UserService(appUserRepository);

    @Test
    void findByEmail_returnsExistingUser() {
        UUID userId = UUID.randomUUID();
        AppUser user = Mockito.mock(AppUser.class);
        given(user.getId()).willReturn(userId);
        given(user.getEmail()).willReturn("alerts@example.com");
        given(user.getCreatedAt()).willReturn(Instant.parse("2026-06-28T00:00:00Z"));
        given(appUserRepository.findByEmail("alerts@example.com")).willReturn(Optional.of(user));

        UserResponse response = service.findByEmail("alerts@example.com");

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("alerts@example.com");
    }

    @Test
    void findByEmail_throwsWhenMissing() {
        given(appUserRepository.findByEmail("missing@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByEmail("missing@example.com"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("missing@example.com");
    }

    @Test
    void create_rejectsDuplicateEmail() {
        AppUser existing = Mockito.mock(AppUser.class);
        given(appUserRepository.findByEmail("alerts@example.com")).willReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.create(new CreateUserRequest("alerts@example.com")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("user already exists");
    }
}
