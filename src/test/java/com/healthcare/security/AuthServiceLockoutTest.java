package com.healthcare.security;

import com.healthcare.dto.LoginRequest;
import com.healthcare.model.Role;
import com.healthcare.model.User;
import com.healthcare.monitoring.SecurityMetrics;
import com.healthcare.repository.UserRepository;
import com.healthcare.security.jwt.JwtTokenProvider;
import com.healthcare.service.AuthService;
import com.healthcare.twofactor.TwoFactorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Verifies brute-force mitigation: an account locks after 5 consecutive
 * failed logins and stays locked until the lock window expires, regardless
 * of whether subsequent attempts use the correct password.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceLockoutTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private TwoFactorService twoFactorService;
    @Mock private SecurityMetrics securityMetrics;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("dr.test");
        user.setPassword("hashed-password");
        user.setRole(Role.DOCTOR);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setFailedLoginAttempts(0);

        when(userRepository.findByUsername("dr.test")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid username or password"));
    }

    @Test
    void accountLocksAfterFiveFailedAttempts() {
        LoginRequest badLogin = new LoginRequest();
        badLogin.setUsername("dr.test");
        badLogin.setPassword("wrong-password");

        for (int i = 0; i < 5; i++) {
            assertThrows(BadCredentialsException.class, () -> authService.login(badLogin));
        }

        assertFalse(user.isAccountNonLocked(), "Account should be locked after 5 failed attempts");
        assertNotNull(user.getLockedUntil());
        assertTrue(user.getLockedUntil().isAfter(LocalDateTime.now()));
    }

    @Test
    void lockedAccount_rejectsEvenBeforeCheckingPassword() {
        user.setAccountNonLocked(false);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(10));

        LoginRequest login = new LoginRequest();
        login.setUsername("dr.test");
        login.setPassword("does-not-matter");

        assertThrows(LockedException.class, () -> authService.login(login));
    }

    @Test
    void expiredLock_automaticallyUnlocksAccount() {
        user.setAccountNonLocked(false);
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1)); // lock window already passed

        LoginRequest login = new LoginRequest();
        login.setUsername("dr.test");
        login.setPassword("wrong-password");

        // Should proceed past the lock check (and then fail on bad credentials, not LockedException)
        assertThrows(BadCredentialsException.class, () -> authService.login(login));
    }
}
