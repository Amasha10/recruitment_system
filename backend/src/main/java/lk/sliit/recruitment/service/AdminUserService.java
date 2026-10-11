package lk.sliit.recruitment.service;

import lk.sliit.recruitment.dto.UserRequest;
import lk.sliit.recruitment.event.AdminActionEvent;
import lk.sliit.recruitment.model.AuditAction;
import lk.sliit.recruitment.model.Role;
import lk.sliit.recruitment.model.User;
import lk.sliit.recruitment.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** User account and role management for the System Administrator. */
@Service
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final ApplicationEventPublisher events;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                            PasswordPolicy passwordPolicy, ApplicationEventPublisher events) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public Page<User> search(String keyword, Role role, Boolean enabled, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return userRepository.search(kw, role, enabled, pageable);
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found (id " + id + ")"));
    }

    public User create(UserRequest form, String adminEmail) {
        String email = form.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new AdminOperationException("A user with email " + email + " already exists");
        }
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            throw new AdminOperationException("Password is required for a new user");
        }
        passwordPolicy.validate(form.getPassword());

        User user = new User();
        user.setFullName(form.getFullName().trim());
        user.setEmail(email);
        user.setPhone(blankToNull(form.getPhone()));
        user.setRole(form.getRole());
        user.setEnabled(form.isEnabled());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        User saved = userRepository.save(user);

        publish(adminEmail, AuditAction.USER_CREATED, saved.getEmail(),
                "Created account with role " + saved.getRole());
        return saved;
    }

    public User update(Long id, UserRequest form, String adminEmail) {
        User user = getById(id);
        String email = form.getEmail().trim().toLowerCase();

        if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmailIgnoreCase(email)) {
            throw new AdminOperationException("Email " + email + " is already used by another account");
        }

        Role oldRole = user.getRole();
        boolean roleChanged = oldRole != form.getRole();
        boolean statusChanged = user.isEnabled() != form.isEnabled();

        if (user.getEmail().equalsIgnoreCase(adminEmail) && !user.getEmail().equalsIgnoreCase(email)) {
            throw new AdminOperationException("You cannot change the email of the account you are logged in with");
        }
        if (user.getEmail().equalsIgnoreCase(adminEmail) && (roleChanged || !form.isEnabled())) {
            throw new AdminOperationException("You cannot change your own role or disable your own account");
        }
        if (oldRole == Role.ADMIN && (roleChanged || !form.isEnabled())) {
            ensureAnotherActiveAdminExists();
        }

        user.setFullName(form.getFullName().trim());
        user.setEmail(email);
        user.setPhone(blankToNull(form.getPhone()));
        user.setRole(form.getRole());
        user.setEnabled(form.isEnabled());

        publish(adminEmail, AuditAction.USER_UPDATED, user.getEmail(), "Profile details updated");
        if (roleChanged) {
            publish(adminEmail, AuditAction.ROLE_CHANGED, user.getEmail(),
                    "Role changed from " + oldRole + " to " + user.getRole());
        }
        if (statusChanged) {
            publish(adminEmail, user.isEnabled() ? AuditAction.USER_ENABLED : AuditAction.USER_DISABLED,
                    user.getEmail(), user.isEnabled() ? "Account enabled" : "Account disabled");
        }
        return user;
    }

    public void toggleEnabled(Long id, String adminEmail) {
        User user = getById(id);
        if (user.getEmail().equalsIgnoreCase(adminEmail)) {
            throw new AdminOperationException("You cannot disable your own account");
        }
        if (user.isEnabled() && user.getRole() == Role.ADMIN) {
            ensureAnotherActiveAdminExists();
        }
        user.setEnabled(!user.isEnabled());
        publish(adminEmail, user.isEnabled() ? AuditAction.USER_ENABLED : AuditAction.USER_DISABLED,
                user.getEmail(), user.isEnabled() ? "Account enabled" : "Account disabled");
    }

    public void resetPassword(Long id, String newPassword, String confirmPassword, String adminEmail) {
        User user = getById(id);
        if (!newPassword.equals(confirmPassword)) {
            throw new AdminOperationException("Passwords do not match");
        }
        passwordPolicy.validate(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        publish(adminEmail, AuditAction.PASSWORD_RESET, user.getEmail(), "Password reset by administrator");
    }

    public void delete(Long id, String adminEmail) {
        User user = getById(id);
        if (user.getEmail().equalsIgnoreCase(adminEmail)) {
            throw new AdminOperationException("You cannot delete your own account");
        }
        if (user.getRole() == Role.ADMIN && user.isEnabled()) {
            ensureAnotherActiveAdminExists();
        }
        userRepository.delete(user);
        publish(adminEmail, AuditAction.USER_DELETED, user.getEmail(),
                "Deleted " + user.getRole() + " account (" + user.getFullName() + ")");
    }

    public void recordLogin(String email) {
        userRepository.findByEmailIgnoreCase(email)
                .ifPresent(u -> u.setLastLoginAt(java.time.LocalDateTime.now()));
    }

    private void ensureAnotherActiveAdminExists() {
        if (userRepository.countByRoleAndEnabled(Role.ADMIN, true) <= 1) {
            throw new AdminOperationException("At least one active administrator must remain in the system");
        }
    }

    private void publish(String admin, AuditAction action, String target, String details) {
        events.publishEvent(new AdminActionEvent(this, admin, action, target, details));
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
