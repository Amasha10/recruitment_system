package lk.sliit.recruitment.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lk.sliit.recruitment.dto.ApiError;
import lk.sliit.recruitment.dto.CurrentUser;
import lk.sliit.recruitment.dto.LoginRequest;
import lk.sliit.recruitment.model.User;
import lk.sliit.recruitment.repository.UserRepository;
import lk.sliit.recruitment.service.AdminUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextRepository;
    private final UserRepository userRepository;
    private final AdminUserService adminUserService;

    public AuthController(AuthenticationManager authenticationManager, SecurityContextRepository contextRepository,
                          UserRepository userRepository, AdminUserService adminUserService) {
        this.authenticationManager = authenticationManager;
        this.contextRepository = contextRepository;
        this.userRepository = userRepository;
        this.adminUserService = adminUserService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest body,
                                   HttpServletRequest request, HttpServletResponse response) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(body.email().trim(), body.password()));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError(401, "This account has been disabled. Contact an administrator."));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError(401, "Invalid email or password"));
        }

        request.getSession(true).invalidate();         
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);

        adminUserService.recordLogin(auth.getName());
        return ResponseEntity.ok(current(auth.getName()));
    }

    @GetMapping("/me")
    public CurrentUser me(Authentication auth) {
        return current(auth.getName());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        new SecurityContextLogoutHandler().logout(request, response, auth);
        return ResponseEntity.noContent().build();
    }

    private CurrentUser current(String email) {
        User u = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        return new CurrentUser(u.getId(), u.getFullName(), u.getEmail(), u.getRole());
    }
}
