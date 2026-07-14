package com.healthcare.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Application user / login account.
 * One user maps to exactly one {@link Role}. If linked to a {@link Patient},
 * patientId stores that relationship so a PATIENT user can only access their own data.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // BCrypt hash, never plaintext

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean accountNonLocked = true;

    private int failedLoginAttempts = 0;

    private LocalDateTime lockedUntil;

    // --- Two-Factor Authentication ---
    @Column(nullable = false)
    private boolean twoFactorEnabled = false;

    @Convert(converter = com.healthcare.encryption.AttributeEncryptor.class)
    private String twoFactorSecret; // Base32 TOTP secret, encrypted at rest

    // Only set when role == PATIENT, links the login account to the clinical record
    private Long patientId;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastLoginAt;
}
