package com.healthcare.model;

import com.healthcare.encryption.AttributeEncryptor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A single clinical encounter/record for a patient.
 * Diagnosis and notes are the most sensitive fields and are encrypted.
 */
@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private String createdByUsername; // doctor/nurse who authored this entry

    @Convert(converter = AttributeEncryptor.class)
    @Column(length = 4000)
    private String diagnosis;

    @Convert(converter = AttributeEncryptor.class)
    @Column(length = 4000)
    private String notes;

    @Convert(converter = AttributeEncryptor.class)
    @Column(length = 2000)
    private String prescription;

    private LocalDateTime recordedAt = LocalDateTime.now();
}
