package lk.sliit.recruitment.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** One row per administrative action. Written by AuditLogListener (Observer pattern). */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String performedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuditAction action;

    @Column(length = 150)
    private String target;

    @Column(length = 500)
    private String details;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected AuditLog() { }

    public AuditLog(String performedBy, AuditAction action, String target, String details) {
        this.performedBy = performedBy;
        this.action = action;
        this.target = target;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getPerformedBy() { return performedBy; }
    public AuditAction getAction() { return action; }
    public String getTarget() { return target; }
    public String getDetails() { return details; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
