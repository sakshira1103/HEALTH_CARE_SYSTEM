package com.healthcare.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.dto.LoginRequest;
import com.healthcare.dto.PatientDTO;
import com.healthcare.dto.RegisterRequest;
import com.healthcare.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end security test: walks through the real HTTP stack (filters,
 * SecurityConfig URL rules, @PreAuthorize checks) rather than mocking
 * individual components. This is the test that actually proves RBAC works,
 * not just that each class compiles in isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void unauthenticatedRequest_toProtectedEndpoint_returns401() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerThenLogin_returnsValidJwt() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("dr.alice");
        register.setPassword("SuperSecurePass123!");
        register.setEmail("alice@hospital.test");
        register.setRole(Role.DOCTOR);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest();
        login.setUsername("dr.alice");
        login.setPassword("SuperSecurePass123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.role").value("DOCTOR"));
    }

    @Test
    void wrongPassword_returnsGenericUnauthorizedMessage() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("nurse.bob");
        register.setPassword("AnotherSecurePass123!");
        register.setEmail("bob@hospital.test");
        register.setRole(Role.NURSE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        LoginRequest badLogin = new LoginRequest();
        badLogin.setUsername("nurse.bob");
        badLogin.setPassword("WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void doctorRole_canAccessPatientEndpoint_afterLogin() throws Exception {
        registerAndLogin("dr.carla", "DoctorPassword123!", Role.DOCTOR);
        String token = loginAndGetToken("dr.carla", "DoctorPassword123!");

        mockMvc.perform(get("/api/patients")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void patientRole_isForbiddenFromCreatingPatients() throws Exception {
        registerAndLogin("patient.dave", "PatientPassword123!", Role.PATIENT);
        String token = loginAndGetToken("patient.dave", "PatientPassword123!");

        PatientDTO dto = new PatientDTO();
        dto.setFullName("Test Patient");

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---

    private void registerAndLogin(String username, String password, Role role) throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setUsername(username);
        register.setPassword(password);
        register.setEmail(username + "@hospital.test");
        register.setRole(role);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsername(username);
        login.setPassword(password);

        String responseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(responseBody).get("accessToken").asText();
    }
}
