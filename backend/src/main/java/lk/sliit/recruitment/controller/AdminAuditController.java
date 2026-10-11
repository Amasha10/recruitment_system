package lk.sliit.recruitment.controller;

import lk.sliit.recruitment.dto.AuditLogResponse;
import lk.sliit.recruitment.dto.PageResponse;
import lk.sliit.recruitment.model.AuditAction;
import lk.sliit.recruitment.repository.AuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
public class AdminAuditController {

    private final AuditLogRepository auditLogRepository;

    public AdminAuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public PageResponse<AuditLogResponse> list(@RequestParam(required = false) AuditAction action,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "15") int size) {
        PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        var result = action == null
                ? auditLogRepository.findAllByOrderByTimestampDesc(pr)
                : auditLogRepository.findByActionOrderByTimestampDesc(action, pr);
        return PageResponse.of(result, AuditLogResponse::from);
    }

    @GetMapping("/actions")
    public List<String> actions() {
        return Arrays.stream(AuditAction.values()).map(Enum::name).toList();
    }
}
