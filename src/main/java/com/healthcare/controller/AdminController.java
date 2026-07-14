package com.healthcare.controller;

import com.healthcare.audit.AuditLogEntry;
import com.healthcare.audit.AuditLogService;
import com.healthcare.model.User;
import com.healthcare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only operations: user management and audit log review.
 * The whole controller is gated to ADMIN at the class level for clarity,
 * even though SecurityConfig also restricts /api/admin/** at the URL level.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/users/{userId}/lock")
    public ResponseEntity<?> lockUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setAccountNonLocked(false);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/unlock")
    public ResponseEntity<?> unlockUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setAccountNonLocked(true);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/audit-logs/user/{username}")
    public ResponseEntity<List<AuditLogEntry>> getLogsForUser(@PathVariable String username) {
        return ResponseEntity.ok(auditLogService.getLogsForUser(username));
    }

    @GetMapping("/audit-logs/resource/{resource}")
    public ResponseEntity<List<AuditLogEntry>> getLogsForResource(@PathVariable String resource) {
        return ResponseEntity.ok(auditLogService.getLogsForResource(resource));
    }
}
