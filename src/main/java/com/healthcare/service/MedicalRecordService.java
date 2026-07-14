package com.healthcare.service;

import com.healthcare.dto.MedicalRecordDTO;
import com.healthcare.model.MedicalRecord;
import com.healthcare.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;

    public List<MedicalRecordDTO> getRecordsForPatient(Long patientId) {
        return medicalRecordRepository.findByPatientId(patientId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public MedicalRecordDTO createRecord(Long patientId, MedicalRecordDTO dto) {
        MedicalRecord record = new MedicalRecord();
        record.setPatientId(patientId);
        record.setDiagnosis(dto.getDiagnosis());
        record.setNotes(dto.getNotes());
        record.setPrescription(dto.getPrescription());
        record.setCreatedByUsername(currentUsername());
        record.setRecordedAt(LocalDateTime.now());

        MedicalRecord saved = medicalRecordRepository.save(record);
        return toDto(saved);
    }

    public MedicalRecordDTO getRecordById(Long recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new NoSuchElementException("Medical record not found: " + recordId));
        return toDto(record);
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    private MedicalRecordDTO toDto(MedicalRecord r) {
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setId(r.getId());
        dto.setPatientId(r.getPatientId());
        dto.setDiagnosis(r.getDiagnosis());
        dto.setNotes(r.getNotes());
        dto.setPrescription(r.getPrescription());
        return dto;
    }
}
