package com.healthcare.service;

import com.healthcare.dto.JwtAuthResponse;
import com.healthcare.dto.LoginRequest;
import com.healthcare.dto.RegisterRequest;
import com.healthcare.model.User;
import com.healthcare.monitoring.SecurityMetrics;
import com.healthcare.repository.UserRepository;
import com.healthcare.security.jwt.JwtTokenProvider;
import com.healthcare.twofactor.TwoFactorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handles the full authentication lifecycle:
 *  - registration (password hashing, role assignment)
 *  - login (credential check -> optional 2FA check -> JWT issuance)
 *  - brute-force mitigation (account lock after repeated failed attempts)
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TwoFactorService twoFactorService;
    private final SecurityMetrics securityMetrics;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public JwtAuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        checkAccountLock(user);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            // Password was correct -> check 2FA if enabled, before issuing a token
            if (user.isTwoFactorEnabled()) {
                if (request.getTwoFactorCode() == null ||
                        !twoFactorService.verifyCode(user.getTwoFactorSecret(), request.getTwoFactorCode())) {
                    throw new BadCredentialsException("Invalid or missing two-factor authentication code");
                }
            }

            resetFailedAttempts(user);
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            securityMetrics.recordLoginSuccess();

            String token = jwtTokenProvider.generateToken(authentication);
            return new JwtAuthResponse(token, user.getUsername(), user.getRole().name());

        } catch (BadCredentialsException ex) {
            registerFailedAttempt(user);
            securityMetrics.recordLoginFailure();
            throw ex;
        }
    }

    private void checkAccountLock(User user) {
        if (!user.isAccountNonLocked()) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                throw new LockedException("Account is locked due to repeated failed login attempts. Try again later.");
            } else {
                // lock window has expired - unlock
                user.setAccountNonLocked(true);
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            }
        }
    }

    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountNonLocked(false);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }
        userRepository.save(user);
    }

    private void resetFailedAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setAccountNonLocked(true);
        user.setLockedUntil(null);
    }
}
