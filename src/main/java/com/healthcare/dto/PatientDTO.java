package com.healthcare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientDTO {
    private Long id;

    @NotBlank
    private String fullName;

    private String nationalId;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String address;
    private String allergies;
    private String bloodGroup;
}
