package lk.sliit.recruitment.dto;

import lk.sliit.recruitment.model.Role;
import lk.sliit.recruitment.model.User;

import java.time.LocalDateTime;

/** What the API returns for a user (never includes the password hash). */
public record UserResponse(Long id, String fullName, String email, String phone,
                           Role role, String roleLabel, boolean enabled,
                           LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastLoginAt) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(),
                u.getRole(), u.getRole().getLabel(), u.isEnabled(),
                u.getCreatedAt(), u.getUpdatedAt(), u.getLastLoginAt());
    }
}
