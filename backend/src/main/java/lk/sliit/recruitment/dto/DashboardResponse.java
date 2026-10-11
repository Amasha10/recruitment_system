package lk.sliit.recruitment.dto;

import lk.sliit.recruitment.model.Role;

import java.util.List;
import java.util.Map;

public record DashboardResponse(long totalUsers, long activeUsers, long disabledUsers,
                                Map<Role, Long> usersByRole,
                                List<UserResponse> recentUsers,
                                List<AuditLogResponse> recentActivity) { }
