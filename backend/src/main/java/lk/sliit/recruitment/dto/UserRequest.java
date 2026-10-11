package lk.sliit.recruitment.dto;

import jakarta.validation.constraints.*;
import lk.sliit.recruitment.model.Role;

/** JSON body for creating (POST) or updating (PUT) a user. Password is only used on create. */
public class UserRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100, message = "Name must be 3-100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @Pattern(regexp = "^$|^(\\+94|0)\\d{9}$", message = "Phone must be like 0771234567 or +94771234567")
    private String phone;

    @NotNull(message = "Select a role")
    private Role role;

    private boolean enabled = true;

    @Size(max = 64)
    private String password;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
