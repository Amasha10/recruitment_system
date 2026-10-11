package lk.sliit.recruitment.dto;

import lk.sliit.recruitment.model.AuditAction;
import lk.sliit.recruitment.model.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(Long id, String performedBy, AuditAction action,
                               String target, String details, LocalDateTime timestamp) {

    public static AuditLogResponse from(AuditLog a) {
        return new AuditLogResponse(a.getId(), a.getPerformedBy(), a.getAction(),
                a.getTarget(), a.getDetails(), a.getTimestamp());
    }
}
