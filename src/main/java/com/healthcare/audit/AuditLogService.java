package com.healthcare.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Persists audit trail entries. Writes are async so audit logging
 * never adds latency to the user-facing request path, while still
 * landing in the database (and structured application logs) for
 * compliance review.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void saveAuditLog(AuditLogEntry entry) {
        auditLogRepository.save(entry);
        log.info("AUDIT | user={} | action={} | target={} | success={} | ip={}",
                entry.getUsername(), entry.getAction(), entry.getTargetResource(),
                entry.isSuccess(), entry.getIpAddress());
    }

    public List<AuditLogEntry> getLogsForUser(String username) {
        return auditLogRepository.findByUsernameOrderByTimestampDesc(username);
    }

    public List<AuditLogEntry> getLogsForResource(String resource) {
        return auditLogRepository.findByTargetResourceOrderByTimestampDesc(resource);
    }
}
