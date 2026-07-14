package com.healthcare.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller/service method whose execution must be recorded
 * in the audit trail. Applied declaratively so engineers can't forget
 * to log a sensitive operation — it's enforced at the method signature level.
 *
 * Usage: @AuditLog(action = "VIEW_PATIENT_RECORD")
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuditLog {
    String action();
}
