package lk.sliit.recruitment.controller;

import lk.sliit.recruitment.dto.AuditLogResponse;
import lk.sliit.recruitment.dto.DashboardResponse;
import lk.sliit.recruitment.dto.UserResponse;
import lk.sliit.recruitment.model.Role;
import lk.sliit.recruitment.repository.AuditLogRepository;
import lk.sliit.recruitment.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminDashboardController(UserRepository userRepository, AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        Map<Role, Long> byRole = new EnumMap<>(Role.class);
        for (Role r : Role.values()) byRole.put(r, userRepository.countByRole(r));

        return new DashboardResponse(
                userRepository.count(),
                userRepository.countByEnabled(true),
                userRepository.countByEnabled(false),
                byRole,
                userRepository.findTop5ByOrderByCreatedAtDesc().stream().map(UserResponse::from).toList(),
                auditLogRepository.findTop8ByOrderByTimestampDesc().stream().map(AuditLogResponse::from).toList());
    }
}
