# Security Policy

## 1. Purpose

This document describes the security controls implemented in the Secure
Healthcare Management System and the rules engineers must follow when
extending it.

## 2. Authentication

- All authentication is via JWT bearer tokens (`Authorization: Bearer <token>`).
- Passwords are hashed with BCrypt (cost factor 10) — never stored or logged
  in plaintext.
- Accounts lock automatically for 15 minutes after 5 consecutive failed
  login attempts (`AuthService.MAX_FAILED_ATTEMPTS`).
- Two-factor authentication (TOTP, RFC 6238) is available per-account and,
  once enabled, is mandatory on every login for that account.
- JWTs expire after 1 hour by default (`app.jwt.expiration-ms`). There is no
  refresh-token flow in this demo — re-authenticate on expiry.

## 3. Authorization

- Role-Based Access Control (RBAC) with five roles: `ADMIN`, `DOCTOR`,
  `NURSE`, `PATIENT`, `RECEPTIONIST`.
- Authorization is enforced at two layers (defense in depth):
  1. Coarse URL-pattern rules in `SecurityConfig`.
  2. Fine-grained `@PreAuthorize` checks on individual controller methods.
- New endpoints **must** have an explicit `@PreAuthorize` — there is no
  "default allow" for anything under `/api/**` other than `/api/auth/**` and
  `/api/public/**`.

## 4. Data Protection

- Sensitive fields (national ID, address, phone, diagnosis, clinical notes,
  prescriptions, 2FA secrets) are encrypted at rest with AES-256-GCM
  (`encryption/EncryptionService.java`).
- Encryption keys must come from a secrets manager in any real deployment —
  never commit a real key to source control. The key in `application.yml` is
  a placeholder for local development only.
- All traffic must be served over TLS in any non-local environment (not
  configured in this demo, which assumes a TLS-terminating reverse proxy in
  front of it).

## 5. Audit Logging

- Every access to a patient record or medical record is logged via the
  `@AuditLog` annotation, regardless of whether the call succeeds or fails.
- Audit entries capture: username, action, target resource, IP address,
  success/failure, timestamp.
- Audit logs are append-only. There is no update or delete endpoint for
  audit entries, by design.

## 6. Input Validation

- All request DTOs use Jakarta Bean Validation (`@NotBlank`, `@Email`,
  `@Size`, etc.). Invalid requests return 400 with field-level error detail.
- The global exception handler (`GlobalExceptionHandler`) ensures internal
  exception details and stack traces are never returned to the client.

## 7. Brute-Force & Enumeration Protection

- Login failure messages are intentionally generic ("Invalid username or
  password") so an attacker cannot distinguish "wrong password" from
  "username doesn't exist."
- Failed attempts trigger account lockout (see Section 2).

## 8. Dependency & Code Hygiene

- Dependencies are pinned to specific versions in `pom.xml`.
- Run `mvn dependency-check:check` (OWASP Dependency-Check plugin, add if not
  present) before any release to catch known-vulnerable libraries.

## 9. Incident Response (informational)

If a security issue is found in this codebase:
1. Do not commit a fix referencing the vulnerability in the commit message.
2. Patch, test, and deploy before public disclosure.
3. Rotate any credentials/keys that may have been exposed.
