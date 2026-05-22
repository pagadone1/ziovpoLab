package com.example.itsupp.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenUtilsTest {

    private final JwtTokenUtils jwtTokenUtils = new JwtTokenUtils();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenUtils, "secret", "testjwtsecretforunittestsmin32characterslong");
        ReflectionTestUtils.setField(jwtTokenUtils, "jwtLifetime", 60_000L);
        ReflectionTestUtils.setField(jwtTokenUtils, "refreshLifetimeDays", 1L);
    }

    @Test
    void generatesAndValidatesAccessToken() {
        var user = new User(
                "testuser",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtTokenUtils.generateToken(user);

        assertTrue(jwtTokenUtils.validateToken(token));
        assertEquals("testuser", jwtTokenUtils.getUsername(token));
        assertEquals(List.of("ROLE_USER"), jwtTokenUtils.getRoles(token));
    }
}
