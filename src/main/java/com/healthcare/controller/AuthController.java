package com.healthcare.controller;

import com.healthcare.audit.AuditLog;
import com.healthcare.dto.JwtAuthResponse;
import com.healthcare.dto.LoginRequest;
import com.healthcare.dto.RegisterRequest;
import com.healthcare.model.User;
import com.healthcare.repository.UserRepository;
import com.healthcare.service.AuthService;
import com.healthcare.twofactor.TwoFactorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TwoFactorService twoFactorService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("username", user.getUsername(), "role", user.getRole().name()));
    }

    @PostMapping("/login")
    @AuditLog(action = "LOGIN_ATTEMPT")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtAuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Step 1 of enabling 2FA: generates a secret + QR code for the user to scan
     * into their authenticator app. The secret is not yet persisted as "enabled"
     * until the user confirms with a valid code via /2fa/confirm.
     */
    @PostMapping("/2fa/setup")
    public ResponseEntity<?> setupTwoFactor(@RequestParam String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        String secret = twoFactorService.generateSecret();
        String qrCodeBase64 = twoFactorService.generateQrCodeImageBase64(username, secret);

        user.setTwoFactorSecret(secret); // encrypted at rest via AttributeEncryptor if mapped
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "secret", secret,
                "qrCodeImageBase64", qrCodeBase64,
                "message", "Scan this QR with your authenticator app, then confirm with /api/auth/2fa/confirm"
        ));
    }

    @PostMapping("/2fa/confirm")
    public ResponseEntity<?> confirmTwoFactor(@RequestParam String username, @RequestParam String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!twoFactorService.verifyCode(user.getTwoFactorSecret(), code)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid verification code"));
        }

        user.setTwoFactorEnabled(true);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Two-factor authentication enabled"));
    }
}
