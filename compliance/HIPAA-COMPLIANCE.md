# HIPAA Compliance Mapping

This document maps HIPAA Security Rule requirements (45 CFR §164.308–§164.312)
to the technical controls implemented in this project. It is for educational
purposes — actual HIPAA compliance requires a full risk assessment, signed
Business Associate Agreements, physical safeguards, and organizational
policies that go beyond any codebase.

## §164.312(a) — Access Control

| Requirement | Implementation |
|---|---|
| Unique user identification | `User.username` is unique; every JWT subject is a specific user id |
| Automatic logoff | JWT expiry (`app.jwt.expiration-ms`, default 1 hour) |
| Encryption and decryption | AES-256-GCM field-level encryption (`EncryptionService`) |

## §164.312(b) — Audit Controls

| Requirement | Implementation |
|---|---|
| Record and examine activity in systems with ePHI | `AuditLogAspect` + `AuditLogEntry` table, capturing user/action/resource/timestamp/IP for every PHI access |

## §164.312(c) — Integrity

| Requirement | Implementation |
|---|---|
| Protect ePHI from improper alteration/destruction | AES-GCM is an *authenticated* cipher — tampering with encrypted data is detected on decryption rather than silently accepted |

## §164.312(d) — Person or Entity Authentication

| Requirement | Implementation |
|---|---|
| Verify identity before granting access | Password (BCrypt) + optional mandatory TOTP 2FA |

## §164.312(e) — Transmission Security

| Requirement | Implementation |
|---|---|
| Guard against unauthorized access during transmission | TLS termination expected at the reverse proxy / load balancer in front of this service (not handled by the app itself) |

## §164.308 — Administrative Safeguards (partial, code-relevant subset)

| Requirement | Implementation |
|---|---|
| Workforce access management | RBAC roles (`ADMIN`, `DOCTOR`, `NURSE`, `PATIENT`, `RECEPTIONIST`) map to job-function-based minimum-necessary access |
| Login monitoring | Account lockout after repeated failed attempts; `healthcare.auth.login.failure` metric |

## Explicitly Out of Scope

This codebase does **not** implement, and a real HIPAA-covered entity would
additionally need:
- Signed Business Associate Agreements with any third-party processor
- Physical safeguards for servers/devices
- Formal risk analysis and management process
- Breach notification procedures
- Workforce training program
- Data backup and disaster recovery plan
- A Business Continuity Plan
