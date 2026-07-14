package com.healthcare.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * Cross-cutting audit logging. Wraps any method annotated with {@link AuditLog}
 * and records the call regardless of whether it succeeds or throws — a failed
 * attempt to access PHI is just as important to log as a successful one
 * (e.g. for detecting brute-force or unauthorized access attempts).
 *
 * Uses @Around (not @AfterReturning) specifically so failures are captured too.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(auditLog)")
    public Object logAuditEvent(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        String username = currentUsername();
        String methodName = joinPoint.getSignature().getName();
        String targetResource = extractTargetResource(joinPoint);
        String ip = currentClientIp();

        AuditLogEntry entry = new AuditLogEntry();
        entry.setUsername(username);
        entry.setAction(auditLog.action());
        entry.setMethod(methodName);
        entry.setTargetResource(targetResource);
        entry.setIpAddress(ip);
        entry.setTimestamp(LocalDateTime.now());

        try {
            Object result = joinPoint.proceed();
            entry.setSuccess(true);
            auditLogService.saveAuditLog(entry);
            return result;
        } catch (Throwable ex) {
            entry.setSuccess(false);
            auditLogService.saveAuditLog(entry);
            throw ex;
        }
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }

    private String currentClientIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /** Best-effort: pulls the first method argument as the "target" identifier for the log entry. */
    private String extractTargetResource(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] != null) {
            return args[0].toString();
        }
        return "n/a";
    }
}
