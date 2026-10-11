package lk.sliit.recruitment.event;

import lk.sliit.recruitment.model.AuditAction;
import org.springframework.context.ApplicationEvent;

/**
 * DESIGN PATTERN: Observer
 *
 * Subject  -> the admin services (AdminUserService, SystemSettingService) publish this event
 *             whenever an administrative action happens.
 * Observer -> any @EventListener that subscribes to it (AuditLogListener, NotificationListener).
 *
 * The services do not know who is listening. New reactions (email alert, notification,
 * statistics) are added by writing a new listener class, without touching the services.
 */
public class AdminActionEvent extends ApplicationEvent {

    private final String performedBy;
    private final AuditAction action;
    private final String target;
    private final String details;

    public AdminActionEvent(Object source, String performedBy, AuditAction action,
                            String target, String details) {
        super(source);
        this.performedBy = performedBy;
        this.action = action;
        this.target = target;
        this.details = details;
    }

    public String getPerformedBy() { return performedBy; }
    public AuditAction getAction() { return action; }
    public String getTarget() { return target; }
    public String getDetails() { return details; }
}
