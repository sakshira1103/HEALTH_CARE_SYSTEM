# Secure Healthcare Management System

A HIPAA-oriented backend demo built with Spring Boot, showing enterprise-grade
security practices: OAuth2-style JWT authentication, role-based access control,
two-factor authentication, field-level encryption, audit logging, caching, and
monitoring.

> ⚠️ **This is a learning/demo project, not a real medical product.** Do not
> use the default secrets in `application.yml` for anything real.

## What's inside

| Feature | Where to look |
|---|---|
| JWT authentication | `security/jwt/` |
| Role-based access control (RBAC) | `@PreAuthorize` annotations in `controller/`, rules in `security/config/SecurityConfig.java` |
| Two-factor authentication (TOTP) | `twofactor/TwoFactorService.java` |
| Field-level encryption (AES-256-GCM) | `encryption/` |
| Audit logging (AOP) | `audit/` |
| Caching | `cache/RedisCacheConfig.java`, `@Cacheable` in `service/PatientService.java` |
| Monitoring | `monitoring/SecurityMetrics.java`, Spring Actuator |
| Tests | `src/test/java/com/healthcare/security/` |

## Requirements

- Java 17+
- Maven 3.8+
- That's it for the default setup — **no Docker, no Redis, no Postgres needed**
  to run it locally. The `dev` profile (active by default) uses an in-memory
  H2 database and a simple in-memory cache.

## Running it

```bash
cd healthcare-system
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

To inspect the in-memory database while it's running, visit
`http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:healthcare_dev`).

## Running the tests

```bash
mvn test
```

This runs:
- `JwtTokenProviderTest` — token issuance/validation/tampering
- `EncryptionServiceTest` — encryption correctness and tamper detection
- `AuthServiceLockoutTest` — brute-force account lockout logic
- `SecurityIntegrationTest` — full HTTP-layer RBAC enforcement

## Trying the API manually

**1. Register a doctor account**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"dr.smith","password":"SuperSecurePassword123!","email":"smith@hospital.test","role":"DOCTOR"}'
```

**2. Log in to get a JWT**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"dr.smith","password":"SuperSecurePassword123!"}'
```
Copy the `accessToken` from the response.

**3. Call a protected endpoint**
```bash
curl http://localhost:8080/api/patients \
  -H "Authorization: Bearer <paste-token-here>"
```

**4. Enable 2FA**
```bash
curl -X POST "http://localhost:8080/api/auth/2fa/setup?username=dr.smith"
```
This returns a `secret` and a `qrCodeImageBase64` (a PNG QR code as base64 —
decode and view it, or scan the secret manually into Google Authenticator).
Then confirm with the 6-digit code your app generates:
```bash
curl -X POST "http://localhost:8080/api/auth/2fa/confirm?username=dr.smith&code=123456"
```
From then on, `/api/auth/login` requires `twoFactorCode` in the request body.

## Switching to a real database

Set these environment variables and change `spring.profiles.active` to
something other than `dev` (or just override the datasource properties):

```bash
export DB_URL=jdbc:postgresql://localhost:5432/healthcare_db
export DB_USERNAME=healthcare_user
export DB_PASSWORD=your-real-password
export JWT_SECRET=$(openssl rand -base64 64)
export ENCRYPTION_KEY=$(openssl rand -base64 32)
```

To turn on real Redis caching instead of the in-memory fallback, set
`app.cache.redis-enabled=true` and provide `spring.data.redis.host`/`port`.

## Project layout

See `docs/security/SECURITY-ARCHITECTURE.md` for a full breakdown of each
security layer, `security-policy.md` for the written policy, and
`compliance/HIPAA-COMPLIANCE.md` for how the technical controls map to HIPAA
requirements.
