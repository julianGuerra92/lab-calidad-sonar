package com.project.citasalud.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        String secureKey = Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes());
        setPrivateField(jwtService, "secretKey", secureKey);
        setPrivateField(jwtService, "jwtExpiration", 1000 * 60 * 60L); // 1 hora
        setPrivateField(jwtService, "refreshExpiration", 1000 * 60 * 60 * 24L); // 1 día
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private UserDetails getMockUser() {
        return User.builder()
                .username("123456789")
                .password("password")
                .authorities(List.of())
                .build();
    }

    @Test
    void shouldGenerateAndValidateToken() {
        UserDetails user = getMockUser();
        String token = jwtService.getToken(user);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, user));
        assertEquals("123456789", jwtService.getDniFromToken(token));
    }

    @Test
    void shouldGenerateRefreshToken() {
        UserDetails user = getMockUser();
        String token = jwtService.getRefreshToken(user);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void shouldReturnFalseForExpiredToken() throws Exception {
        // Forzamos la expiración manualmente (1 segundo en el pasado)
        setPrivateField(jwtService, "jwtExpiration", -1L); // Expira en el pasado

        UserDetails user = getMockUser();
        String token = jwtService.getToken(user);

        boolean isValid = jwtService.isTokenValid(token, user);

        assertFalse(isValid);
    }

    @Test
    void shouldGetCustomClaim() throws Exception {
        UserDetails user = getMockUser();
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");

        long expiration = 1000 * 60 * 60L; // 1 hora
        setPrivateField(jwtService, "jwtExpiration", expiration);

        String token = jwtService.buildToken(claims, user, expiration);

        String role = jwtService.getClaim(token, c -> c.get("role", String.class));
        assertEquals("USER", role);
    }

    @Test
    void shouldGetExpirationDate() {
        UserDetails user = getMockUser();
        String token = jwtService.getToken(user);
        Date expiration = jwtService.getClaim(token, Claims::getExpiration);
        assertNotNull(expiration);
    }
}
