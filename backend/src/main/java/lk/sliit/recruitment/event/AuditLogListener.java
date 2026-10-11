package lk.sliit.recruitment.event;

import lk.sliit.recruitment.model.AuditLog;
import lk.sliit.recruitment.repository.AuditLogRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Observer #1: records every admin action in the audit trail. */
@Component
public class AuditLogListener {

    private final AuditLogRepository auditLogRepository;

    public AuditLogListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @EventListener
    public void onAdminAction(AdminActionEvent event) {
        auditLogRepository.save(new AuditLog(
                event.getPerformedBy(), event.getAction(), event.getTarget(), event.getDetails()));
    }
}
