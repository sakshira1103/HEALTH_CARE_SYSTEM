package com.healthcare.controller;

import com.healthcare.audit.AuditLog;
import com.healthcare.dto.PatientDTO;
import com.healthcare.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Patient record endpoints.
 * Method-level @PreAuthorize is the actual enforcement point (defense in depth
 * on top of the coarse URL rules in SecurityConfig). Every access to a specific
 * patient record is audited via @AuditLog.
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'NURSE', 'RECEPTIONIST')")
    @GetMapping
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'NURSE')")
    @GetMapping("/{patientId}")
    @AuditLog(action = "VIEW_PATIENT_RECORD")
    public ResponseEntity<PatientDTO> getPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientService.getPatientById(patientId));
    }

    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST')")
    @PostMapping
    @AuditLog(action = "CREATE_PATIENT_RECORD")
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientDTO dto) {
        PatientDTO created = patientService.createPatient(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @PutMapping("/{patientId}")
    @AuditLog(action = "UPDATE_PATIENT_RECORD")
    public ResponseEntity<PatientDTO> updatePatient(@PathVariable Long patientId, @Valid @RequestBody PatientDTO dto) {
        return ResponseEntity.ok(patientService.updatePatient(patientId, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{patientId}")
    @AuditLog(action = "DELETE_PATIENT_RECORD")
    public ResponseEntity<Void> deletePatient(@PathVariable Long patientId) {
        patientService.deletePatient(patientId);
        return ResponseEntity.noContent().build();
    }
}
