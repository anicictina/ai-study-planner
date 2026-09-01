package com.anicictina.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.anicictina.backend.user.Role;
import com.anicictina.backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-key-must-be-at-least-32-characters-long";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);
    }

    private User sampleUser() {
        return User.builder()
            .id(1L)
            .email("tina@example.com")
            .role(Role.STUDENT)
            .build();
    }

    @Test
    void generatedTokenContainsOriginalEmail() {
        String token = jwtService.generateToken(sampleUser());

        assertEquals("tina@example.com", jwtService.extractEmail(token));
    }

    @Test
    void generatedTokenContainsOriginalRole() {
        String token = jwtService.generateToken(sampleUser());

        assertEquals("STUDENT", jwtService.extractRole(token));
    }

    @Test
    void freshTokenIsValid() {
        String token = jwtService.generateToken(sampleUser());

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void garbageTokenIsInvalid() {
        assertFalse(jwtService.isTokenValid("not-a-real-jwt"));
    }

    @Test
    void expiredTokenIsInvalid() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String token = jwtService.generateToken(sampleUser());

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void tokenSignedWithDifferentSecretIsInvalid() {
        String token = jwtService.generateToken(sampleUser());

        JwtService otherService = new JwtService();
        ReflectionTestUtils.setField(otherService, "secret", "a-completely-different-secret-key-of-32-chars-min");
        ReflectionTestUtils.setField(otherService, "expirationMs", 3600000L);

        assertFalse(otherService.isTokenValid(token));
    }
}
