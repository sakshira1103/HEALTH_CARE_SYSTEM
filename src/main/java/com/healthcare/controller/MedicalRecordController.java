package com.healthcare.controller;

import com.healthcare.audit.AuditLog;
import com.healthcare.dto.MedicalRecordDTO;
import com.healthcare.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'ADMIN')")
    @GetMapping("/patient/{patientId}")
    @AuditLog(action = "VIEW_MEDICAL_RECORDS")
    public ResponseEntity<List<MedicalRecordDTO>> getRecordsForPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalRecordService.getRecordsForPatient(patientId));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/patient/{patientId}")
    @AuditLog(action = "CREATE_MEDICAL_RECORD")
    public ResponseEntity<MedicalRecordDTO> addMedicalRecord(
            @PathVariable Long patientId,
            @Valid @RequestBody MedicalRecordDTO dto) {
        MedicalRecordDTO created = medicalRecordService.createRecord(patientId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'ADMIN')")
    @GetMapping("/{recordId}")
    @AuditLog(action = "VIEW_SINGLE_MEDICAL_RECORD")
    public ResponseEntity<MedicalRecordDTO> getRecord(@PathVariable Long recordId) {
        return ResponseEntity.ok(medicalRecordService.getRecordById(recordId));
    }
}
