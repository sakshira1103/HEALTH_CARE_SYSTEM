package com.healthcare.model;

/**
 * System roles used for Role-Based Access Control (RBAC).
 * Mapped to Spring Security authorities as "ROLE_<name>".
 */
public enum Role {
    ADMIN,
    DOCTOR,
    NURSE,
    PATIENT,
    RECEPTIONIST
}
