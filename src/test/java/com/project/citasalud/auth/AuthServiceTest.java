package com.project.citasalud.auth;

import com.project.citasalud.codeMFA.VerificationCodeService;
import com.project.citasalud.jwt.JwtService;
import com.project.citasalud.mfa.EmailVerificationService;
import com.project.citasalud.tokenJWT.Token;
import com.project.citasalud.tokenJWT.TokenRepository;
import com.project.citasalud.tokenJWT.TokenType;
import com.project.citasalud.user.User;
import com.project.citasalud.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenRepository tokenRepository;
    @Mock private VerificationCodeService verificationCodeService;
    @Mock private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test //CAMINO FELIZ LOGIN
    void login_ShouldReturnEmailResponse_WhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("123456789", "Password123!");

        User user = User.builder()
                .dni("123456789")
                .email("test@example.com")
                .build();

        when(userRepository.findByDni("123456789")).thenReturn(Optional.of(user));

        EmailResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(emailVerificationService).sendVerificationCode("test@example.com");
    }

    @Test //CAMINO TRISTE LOGIN
    void login_ShouldThrowException_WhenUserNotFound() {
        LoginRequest request = new LoginRequest("999999999", "Password123!");
        when(userRepository.findByDni(request.getDni())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.login(request));
    }

    @Test //CAMINO FELIZ REGISTER
    void register_ShouldSaveUserAndSendEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .dni("123456789")
                .password("Password123!")
                .firstName("Juan")
                .lastName("Pérez")
                .department("Antioquia")
                .city("Medellín")
                .address("Calle 1")
                .email("juan@example.com")
                .numberPhone("3001234567")
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User user = User.builder()
                .dni("123456789")
                .email("juan@example.com")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        EmailResponse response = authService.register(request);

        assertEquals("juan@example.com", response.getEmail());
        verify(emailVerificationService).sendVerificationCode("juan@example.com");
    }

    @Test //CAMINO TRISTE REGISTER
    void register_ShouldThrowException_WhenUserRepositoryFails() {
        RegisterRequest request = RegisterRequest.builder()
                .dni("123456789")
                .password("Password123!")
                .email("juan@example.com")
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    @Test //REFRESH TOKEN
    void refreshToken_ShouldReturnNewTokens_WhenRefreshTokenIsValid() {
        String authHeader = "Bearer validToken";
        String dni = "123456789";

        User user = User.builder().dni(dni).build();

        when(jwtService.getDniFromToken("validToken")).thenReturn(dni);
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("validToken", user)).thenReturn(true);
        when(jwtService.getToken(user)).thenReturn("newAccessToken");

        AuthResponse response = authService.refreshToken(authHeader);

        assertEquals("newAccessToken", response.getToken());
        assertEquals("validToken", response.getRefreshToken());
    }

    @Test //HEADER INVÁLIDO
    void refreshToken_ShouldThrowException_WhenHeaderIsInvalid() {
        String invalidHeader = "InvalidHeader";

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> authService.refreshToken(invalidHeader));

        assertEquals("Invalid bearer token", exception.getMessage());
    }

    @Test //TOKEN INVÁLIDO
    void refreshToken_ShouldThrowException_WhenTokenInvalidForUser() {
        String token = "Bearer invalidToken";
        String dni = "123456789";
        User user = User.builder().dni(dni).build();

        when(jwtService.getDniFromToken("invalidToken")).thenReturn(dni);
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("invalidToken", user)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> authService.refreshToken(token));

        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test //VERIFICAR TOKEN CAMINO FELIZ
    void verifyCodeAndGenerateTokens_ShouldReturnAuthResponse_WhenCodeIsValid() {
        String email = "test@example.com";
        String code = "123456";

        CodeEmailRequest request = new CodeEmailRequest(email, code);
        User user = User.builder().email(email).dni("123").build();

        when(verificationCodeService.verifyCode(email, code)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.getToken(user)).thenReturn("token");
        when(jwtService.getRefreshToken(user)).thenReturn("refresh");

        AuthResponse response = authService.verifyCodeAndGenerateTokens(request);

        assertEquals("token", response.getToken());
        assertEquals("refresh", response.getRefreshToken());
    }

    @Test //VERIFICAR TOKEN CAMINO TRISTE
    void verifyCodeAndGenerateTokens_ShouldThrowException_WhenCodeInvalid() {
        CodeEmailRequest request = new CodeEmailRequest("test@example.com", "wrongCode");

        when(verificationCodeService.verifyCode(request.getEmail(), request.getCode()))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.verifyCodeAndGenerateTokens(request));
    }

    @Test //GUARDAR EL TOKEN DE USUARIO
    void saveUserToken_ShouldSaveTokenCorrectly() {
        User user = User.builder().dni("123456789").build();
        String jwtToken = "mocked.jwt.token";

        authService.saveUserToken(user, jwtToken);

        ArgumentCaptor<Token> captor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(captor.capture());

        Token savedToken = captor.getValue();
        assertEquals(jwtToken, savedToken.getToken());
        assertEquals(user, savedToken.getUser());
        assertFalse(savedToken.isExpired());
        assertFalse(savedToken.isRevoked());
        assertEquals(TokenType.BEARER, savedToken.getTokenType());
    }

    @Test //QUITAR TOKENS A TODOS LOS USUARIOS
    void revokeAllUserTokens_ShouldMarkTokensAsRevokedAndExpired() {
        User user = User.builder().dni("123456789").build();

        Token token1 = Token.builder().revoked(false).expired(false).build();
        Token token2 = Token.builder().revoked(false).expired(false).build();
        List<Token> tokens = List.of(token1, token2);

        when(tokenRepository.findAllValidIsFalseOrRevokedIsFalseByUser_Dni(user.getUsername()))
                .thenReturn(tokens);

        authService.revokeAllUserTokens(user);

        assertTrue(token1.isRevoked());
        assertTrue(token1.isExpired());
        assertTrue(token2.isRevoked());
        assertTrue(token2.isExpired());

        verify(tokenRepository).saveAll(tokens);
    }

    @Test //QUITAR TOKENS CUANDO NO HAY TOKENS VÁLIDOS
    void revokeAllUserTokens_ShouldNotSaveAnything_WhenNoTokensFound() {
        User user = User.builder().dni("123456789").build();
        when(tokenRepository.findAllValidIsFalseOrRevokedIsFalseByUser_Dni(user.getUsername()))
                .thenReturn(List.of());

        authService.revokeAllUserTokens(user);

        verify(tokenRepository, never()).saveAll(any());
    }
}
