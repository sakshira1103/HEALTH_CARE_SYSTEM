package com.healthcare.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Immutable record of a security-relevant action.
 * HIPAA requires that any access to PHI (Protected Health Information)
 * be traceable to a specific user, action, and timestamp. Rows in this
 * table are append-only — there is intentionally no update/delete service method.
 */
@Entity
@Table(name = "audit_log_entries", indexes = {
        @Index(name = "idx_audit_username", columnList = "username"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String method;

    private String targetResource; // e.g. "patient:1001"

    private String ipAddress;

    @Column(nullable = false)
    private boolean success = true;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
