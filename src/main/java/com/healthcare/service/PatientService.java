package com.healthcare.service;

import com.healthcare.dto.PatientDTO;
import com.healthcare.model.Patient;
import com.healthcare.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Patient CRUD. Reads are cached in Redis under the "patients" cache name
 * because patient demographic lookups happen far more often than writes —
 * a classic read-heavy workload that benefits from caching.
 * Any write evicts the relevant cache entry so stale data is never served.
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    @Cacheable(value = "patients", key = "#patientId")
    public PatientDTO getPatientById(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NoSuchElementException("Patient not found: " + patientId));
        return toDto(patient);
    }

    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "patients", key = "#result.id")
    public PatientDTO createPatient(PatientDTO dto) {
        Patient patient = fromDto(dto);
        patient.setCreatedAt(LocalDateTime.now());
        patient.setUpdatedAt(LocalDateTime.now());
        Patient saved = patientRepository.save(patient);
        return toDto(saved);
    }

    @CacheEvict(value = "patients", key = "#patientId")
    public PatientDTO updatePatient(Long patientId, PatientDTO dto) {
        Patient existing = patientRepository.findById(patientId)
                .orElseThrow(() -> new NoSuchElementException("Patient not found: " + patientId));

        existing.setFullName(dto.getFullName());
        existing.setNationalId(dto.getNationalId());
        existing.setDateOfBirth(dto.getDateOfBirth());
        existing.setPhoneNumber(dto.getPhoneNumber());
        existing.setAddress(dto.getAddress());
        existing.setAllergies(dto.getAllergies());
        existing.setBloodGroup(dto.getBloodGroup());
        existing.setUpdatedAt(LocalDateTime.now());

        Patient saved = patientRepository.save(existing);
        return toDto(saved);
    }

    @CacheEvict(value = "patients", key = "#patientId")
    public void deletePatient(Long patientId) {
        patientRepository.deleteById(patientId);
    }

    private PatientDTO toDto(Patient p) {
        PatientDTO dto = new PatientDTO();
        dto.setId(p.getId());
        dto.setFullName(p.getFullName());
        dto.setNationalId(p.getNationalId());
        dto.setDateOfBirth(p.getDateOfBirth());
        dto.setPhoneNumber(p.getPhoneNumber());
        dto.setAddress(p.getAddress());
        dto.setAllergies(p.getAllergies());
        dto.setBloodGroup(p.getBloodGroup());
        return dto;
    }

    private Patient fromDto(PatientDTO dto) {
        Patient p = new Patient();
        p.setFullName(dto.getFullName());
        p.setNationalId(dto.getNationalId());
        p.setDateOfBirth(dto.getDateOfBirth());
        p.setPhoneNumber(dto.getPhoneNumber());
        p.setAddress(dto.getAddress());
        p.setAllergies(dto.getAllergies());
        p.setBloodGroup(dto.getBloodGroup());
        return p;
    }
}
