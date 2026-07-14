# Sample Audit Log Output

This is a representative sample of what `audit_log_entries` rows look like
once the application is running (formatted here for readability; actual rows
are plain DB records, also echoed to the application log at INFO level).

```
2026-06-21 09:12:03 | AUDIT | user=dr.smith     | action=LOGIN_ATTEMPT           | target=n/a          | success=true  | ip=127.0.0.1
2026-06-21 09:13:41 | AUDIT | user=dr.smith     | action=VIEW_PATIENT_RECORD     | target=1001         | success=true  | ip=127.0.0.1
2026-06-21 09:14:15 | AUDIT | user=dr.smith     | action=CREATE_MEDICAL_RECORD   | target=1001         | success=true  | ip=127.0.0.1
2026-06-21 09:20:02 | AUDIT | user=nurse.jones  | action=VIEW_MEDICAL_RECORDS    | target=1001         | success=true  | ip=127.0.0.1
2026-06-21 09:25:47 | AUDIT | user=unknown      | action=LOGIN_ATTEMPT           | target=n/a          | success=false | ip=203.0.113.42
2026-06-21 09:25:51 | AUDIT | user=unknown      | action=LOGIN_ATTEMPT           | target=n/a          | success=false | ip=203.0.113.42
2026-06-21 10:02:09 | AUDIT | user=patient.dave | action=VIEW_PATIENT_RECORD     | target=2002         | success=false | ip=198.51.100.7
2026-06-21 10:15:30 | AUDIT | user=admin        | action=DELETE_PATIENT_RECORD   | target=3003         | success=true  | ip=10.0.0.5
```

Note the failed `VIEW_PATIENT_RECORD` from `patient.dave` — that's exactly
the kind of event this system is designed to surface: a PATIENT-role user
attempted to view a record they're not authorized for, was rejected by
`@PreAuthorize`, and the rejected attempt was still recorded (the `@Around`
audit aspect logs on the exception path, not just on success).
