package com.healthcare.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {
    List<AuditLogEntry> findByUsernameOrderByTimestampDesc(String username);
    List<AuditLogEntry> findByTargetResourceOrderByTimestampDesc(String targetResource);
}
