package lk.sliit.recruitment.event;

import lk.sliit.recruitment.model.AuditAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

/**
 * Observer #2: informs the affected user about changes to their account.
 * Logs to the console for now; plug in the system's notification module or email here.
 */
@Component
public class AccountNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(AccountNotificationListener.class);

    private static final Set<AuditAction> USER_FACING = EnumSet.of(
            AuditAction.USER_CREATED, AuditAction.ROLE_CHANGED, AuditAction.USER_DISABLED,
            AuditAction.USER_ENABLED, AuditAction.PASSWORD_RESET);

    @EventListener
    public void onAdminAction(AdminActionEvent event) {
        if (USER_FACING.contains(event.getAction())) {
            log.info("[NOTIFY] {} -> {}: {}", event.getTarget(), event.getAction(), event.getDetails());
        }
    }
}
