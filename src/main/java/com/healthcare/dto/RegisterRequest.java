package com.healthcare.dto;

import com.healthcare.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 12, message = "Password must be at least 12 characters")
    private String password;

    @NotBlank
    @Email
    private String email;

    @NotNull
    private Role role;
}
