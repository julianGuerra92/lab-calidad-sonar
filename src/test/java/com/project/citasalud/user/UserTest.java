package com.project.citasalud.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class UserTest {
    @Test
    void testUserDetailsMethods() {
        User user = User.builder()
                .dni("123456789")
                .password("securePass")
                .role(Role.USER)
                .build();

        assertEquals("123456789", user.getUsername());
        assertEquals("securePass", user.getPassword());
        assertEquals(List.of(new SimpleGrantedAuthority("USER")), user.getAuthorities());

        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }
}
