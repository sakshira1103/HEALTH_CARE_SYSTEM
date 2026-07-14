package com.healthcare.model;

import com.healthcare.encryption.AttributeEncryptor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Core patient record. Direct identifiers and sensitive fields are
 * encrypted at rest using AES-256-GCM via {@link AttributeEncryptor}.
 * Non-sensitive demographic/operational fields (e.g. id, createdAt) stay
 * plaintext so they remain indexable/sortable.
 */
@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = AttributeEncryptor.class)
    @Column(nullable = false)
    private String fullName;

    @Convert(converter = AttributeEncryptor.class)
    @Column(unique = true)
    private String nationalId; // SSN / Aadhaar-equivalent — highly sensitive

    private LocalDate dateOfBirth;

    @Convert(converter = AttributeEncryptor.class)
    private String phoneNumber;

    @Convert(converter = AttributeEncryptor.class)
    private String address;

    @Convert(converter = AttributeEncryptor.class)
    @Column(length = 2000)
    private String allergies;

    private String bloodGroup; // low sensitivity, kept plaintext for fast filtering

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}
