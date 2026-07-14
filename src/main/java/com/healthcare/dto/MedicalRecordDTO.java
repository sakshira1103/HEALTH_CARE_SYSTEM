package com.healthcare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MedicalRecordDTO {
    private Long id;
    private Long patientId;

    @NotBlank
    private String diagnosis;

    private String notes;
    private String prescription;
}
