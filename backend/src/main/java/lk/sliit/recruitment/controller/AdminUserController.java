package lk.sliit.recruitment.controller;

import jakarta.validation.Valid;
import lk.sliit.recruitment.dto.PageResponse;
import lk.sliit.recruitment.dto.PasswordResetRequest;
import lk.sliit.recruitment.dto.UserRequest;
import lk.sliit.recruitment.dto.UserResponse;
import lk.sliit.recruitment.model.Role;
import lk.sliit.recruitment.service.AdminUserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    private final AdminUserService userService;

    public AdminUserController(AdminUserService userService) { this.userService = userService; }

    @GetMapping("/roles")
    public List<Map<String, String>> roles() {
        return Arrays.stream(Role.values())
                .map(r -> Map.of("value", r.name(), "label", r.getLabel()))
                .toList();
    }

    @GetMapping("/users")
    public PageResponse<UserResponse> list(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Role role,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        Boolean enabled = "active".equals(status) ? Boolean.TRUE
                        : "disabled".equals(status) ? Boolean.FALSE : null;
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.of(userService.search(keyword, role, enabled, pageable), UserResponse::from);
    }

    @GetMapping("/users/{id}")
    public UserResponse get(@PathVariable Long id) {
        return UserResponse.from(userService.getById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest body, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.from(userService.create(body, auth.getName())));
    }

    @PutMapping("/users/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest body, Authentication auth) {
        return UserResponse.from(userService.update(id, body, auth.getName()));
    }

    @PatchMapping("/users/{id}/status")
    public UserResponse toggleStatus(@PathVariable Long id, Authentication auth) {
        userService.toggleEnabled(id, auth.getName());
        return UserResponse.from(userService.getById(id));
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long id,
                                                             @Valid @RequestBody PasswordResetRequest body,
                                                             Authentication auth) {
        userService.resetPassword(id, body.newPassword(), body.confirmPassword(), auth.getName());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        userService.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
