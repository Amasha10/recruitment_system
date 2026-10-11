package lk.sliit.recruitment.repository;

import lk.sliit.recruitment.model.AuditAction;
import lk.sliit.recruitment.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<AuditLog> findByActionOrderByTimestampDesc(AuditAction action, Pageable pageable);

    List<AuditLog> findTop8ByOrderByTimestampDesc();
}
