package lk.sliit.recruitment.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @NotBlank(message = "New password is required") String newPassword,
        @NotBlank(message = "Please confirm the password") String confirmPassword) { }
