# Security Architecture

## Request Lifecycle (authenticated request)

```
Client -> JwtAuthenticationFilter -> SecurityConfig URL rules -> @PreAuthorize -> Controller/Service
```

1. Client sends `Authorization: Bearer <jwt>`.
2. `JwtAuthenticationFilter` validates the token, loads the user, and
   populates `SecurityContextHolder`.
3. `SecurityConfig`'s URL-pattern rules do a coarse first check (e.g. is this
   path even reachable without authentication?).
4. `@PreAuthorize` on the specific controller method does the fine-grained
   role check.
5. If everything passes, the controller/service runs. If the method is
   annotated `@AuditLog`, the call (success or failure) is recorded.

## Why two authorization layers?

- **URL rules** (`SecurityConfig.filterChain`) are a fast, cheap first filter
  — e.g. nobody who isn't ADMIN even reaches an `/api/admin/**` controller.
- **`@PreAuthorize`** on individual methods is the actual fine-grained
  decision — e.g. `DOCTOR` and `NURSE` can both reach `/api/medical-records/**`
  by URL, but only `DOCTOR` can call `addMedicalRecord`.

This is deliberate defense-in-depth: a mistake in one layer doesn't expose
the whole system.

## Why JWT instead of server-side sessions?

- Stateless: no session store, so the API scales horizontally without sticky
  sessions or shared session storage.
- The token carries the role as a claim, so authorization checks don't need
  a database round-trip on every request just to know "what can this user do."
- Trade-off: a JWT can't be instantly revoked before its expiry (mitigated
  here by a short 1-hour expiry; a real system might add a token-blacklist
  cache for emergency revocation).

## Why AES-GCM for field encryption (not just AES-CBC)?

GCM is an *authenticated* encryption mode — it produces a tag that detects
any tampering with the ciphertext. CBC alone offers confidentiality but not
integrity; an attacker with database write access could flip bits in CBC
ciphertext and the application might decrypt to corrupted-but-valid-looking
data without ever noticing. GCM throws on decrypt instead.

## Why audit logging via AOP instead of manual logging calls in each method?

Putting `@AuditLog(action = "...")` on a method, with a single `@Around`
aspect catching it, means:
- A developer can't "forget" to log a sensitive action covered by the
  annotation — it's structural, not a discipline problem.
- Logging logic (who, when, success/failure, IP) lives in exactly one place
  (`AuditLogAspect`), so the audit trail format is consistent everywhere
  it's used.
- The aspect wraps with `@Around` (not `@AfterReturning`) specifically so a
  *failed* access attempt (e.g. an exception thrown mid-method) is logged
  too — failed PHI access attempts are often more security-relevant than
  successful ones.

## Why cache patient lookups but not medical record writes?

Read-heavy, write-light data (patient demographics looked up dozens of times
per single update) benefits from caching; the cache is explicitly evicted on
every write (`@CacheEvict`) so no client ever sees stale-but-cached PHI after
an update.

## Known limitations of this demo (be upfront about these)

- No refresh-token / token-revocation flow.
- No rate limiting at the application layer (expected to sit behind an API
  gateway / WAF in production).
- The encryption key and JWT secret in `application.yml` are placeholders —
  swapping them for environment variables is mandatory before any real use.
- The 2FA setup/confirm endpoints in `AuthController` take a `username` query
  parameter directly rather than deriving it from the caller's own
  authenticated session — acceptable for a learning project, not for
  production (a real version should restrict these to "the currently logged
  in user" only).
