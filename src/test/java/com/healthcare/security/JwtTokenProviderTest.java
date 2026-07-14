package com.healthcare.security;

import com.healthcare.security.jwt.JwtTokenProvider;
import com.healthcare.security.service.UserPrincipal;
import com.healthcare.model.Role;
import com.healthcare.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the JWT provider issues tokens that round-trip correctly,
 * rejects tampered/invalid tokens, and correctly identifies expired tokens.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "test-secret-key-for-junit-tests-only-not-for-production-use-0123456789";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000L);
    }

    private Authentication buildAuth(Long userId, String username, Role role) {
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setPassword("irrelevant-hash");
        user.setRole(role);
        UserPrincipal principal = new UserPrincipal(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    void generatedToken_isValid() {
        Authentication auth = buildAuth(1L, "dr.smith", Role.DOCTOR);
        String token = jwtTokenProvider.generateToken(auth);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void userIdExtractedFromToken_matchesOriginalUser() {
        Authentication auth = buildAuth(42L, "nurse.jones", Role.NURSE);
        String token = jwtTokenProvider.generateToken(auth);

        Long extractedId = jwtTokenProvider.getUserIdFromToken(token);
        assertEquals(42L, extractedId);
    }

    @Test
    void tamperedToken_failsValidation() {
        Authentication auth = buildAuth(1L, "admin", Role.ADMIN);
        String token = jwtTokenProvider.generateToken(auth);

        // flip a character in the signature portion
        String tampered = token.substring(0, token.length() - 5) + "AAAAA";

        assertFalse(jwtTokenProvider.validateToken(tampered));
    }

    @Test
    void malformedToken_failsValidation() {
        assertFalse(jwtTokenProvider.validateToken("not.a.valid.jwt"));
    }

    @Test
    void expiredToken_failsValidation() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", -1000L); // already expired
        Authentication auth = buildAuth(1L, "dr.smith", Role.DOCTOR);
        String token = jwtTokenProvider.generateToken(auth);

        assertFalse(jwtTokenProvider.validateToken(token));
    }
}
